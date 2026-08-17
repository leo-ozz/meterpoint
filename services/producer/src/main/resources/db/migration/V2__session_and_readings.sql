-- V2: charging session lifecycle and meter readings.
-- Rationale and handler contract: docs/schema-v2.md. V1 is never edited.

CREATE SEQUENCE transaction_id_seq AS INTEGER START WITH 1000;

CREATE TABLE session (
    id                    BIGINT      GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    charge_point_id       TEXT        NOT NULL,
    transaction_id        INTEGER     NOT NULL DEFAULT nextval('transaction_id_seq'),
    connector_id          INTEGER     NOT NULL,

    start_id_tag          TEXT        NOT NULL,
    stop_id_tag           TEXT,

    -- Cumulative lifetime registers in Wh, as received. NULL meter_stop = no stop message.
    meter_start           TEXT        NOT NULL,
    meter_stop            TEXT,

    stop_reason           TEXT,
    state                 TEXT        NOT NULL,

    start_message_id      UUID        NOT NULL,
    stop_message_id       UUID,

    -- occurred_at = charge point clock, skewed, not trusted for ordering.
    -- received_at = server clock, trusted.
    started_occurred_at   TIMESTAMPTZ NOT NULL,
    started_received_at   TIMESTAMPTZ NOT NULL,
    stopped_occurred_at   TIMESTAMPTZ,
    stopped_received_at   TIMESTAMPTZ,

    created_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT session_state_valid
        CHECK (state IN ('ACTIVE', 'STOPPED')),

    -- Stop fields arrive together or not at all.
    CONSTRAINT session_stop_fields_consistent
        CHECK (
            (state = 'ACTIVE'  AND meter_stop IS NULL
             AND stopped_occurred_at IS NULL
             AND stopped_received_at IS NULL
             AND stop_message_id IS NULL)
             OR (state = 'STOPPED' AND meter_stop IS NOT NULL
             AND stopped_occurred_at IS NOT NULL
             AND stopped_received_at IS NOT NULL
             AND stop_message_id IS NOT NULL)
            ),

    CONSTRAINT session_connector_positive
        CHECK (connector_id >= 1)
);

-- Natural key. transaction_id alone is not unique across the fleet.
CREATE UNIQUE INDEX session_natural_key_idx
    ON session (charge_point_id, transaction_id);

CREATE UNIQUE INDEX session_start_message_id_idx
    ON session (charge_point_id, start_message_id);

CREATE UNIQUE INDEX session_stop_message_id_idx
    ON session (charge_point_id, stop_message_id)
    WHERE stop_message_id IS NOT NULL;

-- At most one active session per connector, not per charge point.
CREATE UNIQUE INDEX session_one_active_per_connector_idx
    ON session (charge_point_id, connector_id)
    WHERE state = 'ACTIVE';

-- Append-only. No foreign key to session: readings may arrive for a session that never existed.
CREATE TABLE reading (
    id                BIGINT      GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    -- Session natural key carried on the row; resolved in the warehouse, not here.
    charge_point_id   TEXT        NOT NULL,
    transaction_id    INTEGER     NOT NULL,

    occurred_at       TIMESTAMPTZ NOT NULL,
    received_at       TIMESTAMPTZ NOT NULL,

    -- Raw. May be non-numeric. Parsing is a warehouse concern.
    value             TEXT        NOT NULL,

    -- NULL means absent on the wire, which is not the same as a default value.
    measurand         TEXT,
    unit              TEXT,
    context           TEXT,

    source            TEXT        NOT NULL,
    message_id        UUID        NOT NULL,

    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT reading_source_valid
        CHECK (source IN ('METER_VALUES', 'TRANSACTION_DATA'))
);

-- Write-boundary deduplication. source is deliberately not in the key.
CREATE UNIQUE INDEX reading_natural_key_idx
    ON reading (charge_point_id, transaction_id, occurred_at);