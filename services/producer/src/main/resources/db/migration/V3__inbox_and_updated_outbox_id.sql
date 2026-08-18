-- V3: idempotent receiver, superseded session state, outbox ordering index.
-- Rationale: docs/schema-v3.md. V1 and V2 are never edited.

-- Envelope-layer deduplication. Handler runs only on first execution.
-- Response body stored so retries receive a byte-identical reply,
-- including the server-assigned transaction_id.
-- Written in the same transaction as the domain rows: a handler failure
-- rolls the inbox row back and the retry re-executes.
-- Prunable: retry windows are minutes, not the lifetime of a session.
CREATE TABLE inbox (
                       charge_point_id  TEXT        NOT NULL,
                       message_id       UUID        NOT NULL,
                       action           TEXT        NOT NULL,
                       payload_hash     BYTEA       NOT NULL,
                       response_body    JSONB,
                       received_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
                       PRIMARY KEY (charge_point_id, message_id),
                       CONSTRAINT inbox_action_valid
                           CHECK (action IN ('StartTransaction', 'MeterValues', 'StopTransaction'))
    );

-- Retention scan support.
CREATE INDEX inbox_received_at_idx ON inbox (received_at);

-- SUPERSEDED: a StartTransaction arrived on a connector whose prior session
-- was still ACTIVE. The stop was never received. The orphan is closed by
-- inference, not by the charge point.
ALTER TABLE session DROP CONSTRAINT session_state_valid;
ALTER TABLE session ADD CONSTRAINT session_state_valid
    CHECK (state IN ('ACTIVE', 'STOPPED', 'SUPERSEDED'));

-- ACTIVE:     no stop fields.
-- STOPPED:    all stop fields present.
-- SUPERSEDED: stop fields all-absent (inferred close) or all-present
--             (late StopTransaction arrived after the supersede).
ALTER TABLE session DROP CONSTRAINT session_stop_fields_consistent;
ALTER TABLE session ADD CONSTRAINT session_stop_fields_consistent
    CHECK (
        (state = 'ACTIVE'
            AND meter_stop IS NULL
            AND stopped_occurred_at IS NULL
            AND stopped_received_at IS NULL
            AND stop_message_id IS NULL)
            OR (state = 'STOPPED'
            AND meter_stop IS NOT NULL
            AND stopped_occurred_at IS NOT NULL
            AND stopped_received_at IS NOT NULL
            AND stop_message_id IS NOT NULL)
            OR (state = 'SUPERSEDED'
            AND ((meter_stop IS NULL
                AND stopped_occurred_at IS NULL
                AND stopped_received_at IS NULL
                AND stop_message_id IS NULL)
                OR (meter_stop IS NOT NULL
                    AND stopped_occurred_at IS NOT NULL
                    AND stopped_received_at IS NOT NULL
                    AND stop_message_id IS NOT NULL)))
        );

-- O2: ordering is guaranteed per (aggregate_type, aggregate_id), not per
-- aggregate_id alone. V1's index worked only because UUIDs do not collide
-- across types; it did not express the invariant. id remains the trailing
-- column: it is the insertion-order sort the poller reads in.
DROP INDEX outbox_unpublished_idx;
CREATE INDEX outbox_unpublished_idx
    ON outbox (aggregate_type, aggregate_id, id)
    WHERE published_at IS NULL;