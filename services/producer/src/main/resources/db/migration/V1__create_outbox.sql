-- V1__create_outbox.sql
--
-- Transactional outbox for domain events. Rows are written in the same
-- transaction as the state change they describe, and drained by a poller
-- using claim-based polling (SELECT ... FOR UPDATE SKIP LOCKED).
--
-- Delivery is at-least-once: a publish may succeed and its marking
-- transaction fail, redelivering the event. Consumers deduplicate on
-- event_id.

CREATE TABLE outbox (
    id              BIGINT      GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    event_id        UUID        NOT NULL UNIQUE,

    aggregate_type  TEXT        NOT NULL,
    aggregate_id    UUID        NOT NULL,
    event_type      TEXT        NOT NULL,

    schema_version  INT         NOT NULL,
    payload         JSONB       NOT NULL,

    occurred_at     TIMESTAMPTZ NOT NULL,
    received_at     TIMESTAMPTZ NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    published_at    TIMESTAMPTZ,

    attempt_count   INT         NOT NULL DEFAULT 0,
    last_error      TEXT
);

COMMENT ON COLUMN outbox.id             IS 'Storage ordering only. NOT safe as a poller high-water mark: sequence values are allocated at insert time but rows become visible at commit time, so WHERE id > last_seen skips rows under concurrency.';
COMMENT ON COLUMN outbox.event_id       IS 'Consumer-visible identity. Generated in application code (UUIDv7). Travels in the payload; the warehouse deduplicates on it.';
COMMENT ON COLUMN outbox.occurred_at    IS 'Business time as reported by the charge point. Clock may be skewed; not trustworthy for ordering.';
COMMENT ON COLUMN outbox.received_at    IS 'Server time the originating message was received. Trusted clock.';
COMMENT ON COLUMN outbox.created_at     IS 'Row insert time. Diverges from received_at when an event is derived rather than message-triggered (e.g. a reaped session close).';
COMMENT ON COLUMN outbox.published_at   IS 'Set when the payload reached the sink and the marking transaction committed. NULL means claimable.';
COMMENT ON COLUMN outbox.attempt_count  IS 'Reserved. Not incremented by the current poller: an increment inside the claim transaction rolls back with the failure it was recording.';

-- Claim index. Partial, so it contains only unpublished rows: its size is
-- proportional to the backlog, not to history. Rows leave the index when
-- published, so it self-maintains and needs no bloat management.
--
-- The planner only uses this if the query predicate literally matches the
-- index predicate. A parameterised or rephrased equivalent silently
-- sequential-scans instead.
CREATE INDEX outbox_unpublished_idx
    ON outbox (aggregate_id, id)
    WHERE published_at IS NULL;
