#!/usr/bin/env bash
set -euo pipefail

# Dumps the meterpoint database to the backup directory on the data volume.
# Off-site copy is a manual pull from a trusted machine

PROJECT_DIR="$HOME/meterpoint"
BACKUP_DIR="/mnt/meterpoint/backups"
KEEP=14                      # 14 days
LOG="$BACKUP_DIR/backup.log"

exec >>"$LOG" 2>&1
echo "=== $(date -u +%Y-%m-%dT%H:%M:%SZ) starting ==="

# Refuse to run if the volume is not mounted: writing to the root disk
# would silently produce backups that vanish on the next mount.
mountpoint -q /mnt/meterpoint || { echo "FAIL: data volume not mounted"; exit 1; }

cd "$PROJECT_DIR"
export COMPOSE_ENV_FILES="$HOME/.meterpoint.env,$PROJECT_DIR/.env.ci"

STAMP=$(date -u +%Y%m%dT%H%M%SZ)
DUMP="$BACKUP_DIR/meterpoint-${STAMP}.dump"

# dump
docker compose exec -T postgres \
  sh -c 'pg_dump -U "$POSTGRES_USER" -d "$POSTGRES_DB" -Fc --no-owner --no-privileges' \
  > "$DUMP"

SIZE=$(stat -c %s "$DUMP")
if [ "$SIZE" -lt 1024 ]; then
  echo "FAIL: dump is ${SIZE} bytes"
  exit 1
fi

# Verify
docker compose exec -T postgres pg_restore --list - < "$DUMP" > /dev/null
echo "OK: $DUMP (${SIZE} bytes), table of contents readable"

# Retention - delete beyond KEEP.
ls -1t "$BACKUP_DIR"/meterpoint-*.dump 2>/dev/null \
  | tail -n +$((KEEP + 1)) \
  | while read -r old; do
      echo "pruning $old"
      rm -f "$old"
    done

echo "=== $(date -u +%Y-%m-%dT%H:%M:%SZ) done ==="
