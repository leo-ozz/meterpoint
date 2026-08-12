#!/usr/bin/env bash
set -euo pipefail

# Restores the most recent dump into a scratch database and verifies the
# schema matches the live database. Run at the weekly gate.
# Leaves no scratch database behind, including on failure.

PROJECT_DIR="$HOME/meterpoint"
BACKUP_DIR="/mnt/meterpoint/backups"
SCRATCH_DB="meterpoint_restore_test"

cd "$PROJECT_DIR"
export COMPOSE_ENV_FILES="$HOME/.meterpoint.env,$PROJECT_DIR/.env.ci"

pg() { docker compose exec -T postgres sh -c "$1"; }

cleanup() {
  pg "dropdb -U \"\$POSTGRES_USER\" --if-exists $SCRATCH_DB" >/dev/null 2>&1 || true
}
trap cleanup EXIT

DUMP=$(ls -1t "$BACKUP_DIR"/meterpoint-*.dump 2>/dev/null | head -1)
[ -n "$DUMP" ] || { echo "FAIL: no dump found in $BACKUP_DIR"; exit 1; }
echo "verifying: $DUMP"

# Restore into a scratch database. --exit-on-error is essential: without it
# pg_restore continues past errors and exits zero, so a partial restore
# would report success.
cleanup
pg "createdb -U \"\$POSTGRES_USER\" $SCRATCH_DB"
docker compose exec -T postgres \
  sh -c "pg_restore -U \"\$POSTGRES_USER\" -d $SCRATCH_DB --exit-on-error" < "$DUMP"

# Compare Flyway history. Checksums prove the schema is identical, not merely present.
LIVE=$(pg "psql -U \"\$POSTGRES_USER\" -d $POSTGRES_DB -At -c \"select version||':'||checksum from flyway_schema_history where success order by installed_rank;\"")
REST=$(pg "psql -U \"\$POSTGRES_USER\" -d $SCRATCH_DB -At -c \"select version||':'||checksum from flyway_schema_history where success order by installed_rank;\"")

if [ -z "$REST" ]; then
  echo "FAIL: restored database has no successful migrations"
  exit 1
fi

if [ "$LIVE" != "$REST" ]; then
  echo "FAIL: migration history differs"
  echo "live:     $LIVE"
  echo "restored: $REST"
  exit 1
fi
echo "OK: migration history matches ($(echo "$LIVE" | wc -l) migrations)"

# The partial index must survive the round trip. A non-partial index would
# be a silent performance regression in the outbox poller.
IDX=$(pg "psql -U \"\$POSTGRES_USER\" -d $SCRATCH_DB -At -c \"select indexdef from pg_indexes where indexname = 'outbox_unpublished_idx';\"")
case "$IDX" in
  *"WHERE (published_at IS NULL)"*) echo "OK: partial index present" ;;
  "") echo "FAIL: outbox_unpublished_idx missing"; exit 1 ;;
  *) echo "FAIL: index is not partial: $IDX"; exit 1 ;;
esac

# Row counts. Currently zero everywhere; meaningful from week 2 onward.
LIVE_ROWS=$(pg "psql -U \"\$POSTGRES_USER\" -d $POSTGRES_DB -At -c \"select count(*) from outbox;\"")
REST_ROWS=$(pg "psql -U \"\$POSTGRES_USER\" -d $SCRATCH_DB -At -c \"select count(*) from outbox;\"")
echo "outbox rows: live=$LIVE_ROWS restored=$REST_ROWS (live may have grown since the dump)"

echo "RESTORE VERIFIED"
