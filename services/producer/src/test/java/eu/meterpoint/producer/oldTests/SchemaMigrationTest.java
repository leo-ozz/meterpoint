//package eu.meterpoint.producer.oldTests;
//
//import eu.meterpoint.producer.TestcontainersConfiguration;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.context.annotation.Import;
//import org.springframework.dao.DataIntegrityViolationException;
//import org.springframework.jdbc.core.JdbcTemplate;
//
//import java.time.OffsetDateTime;
//import java.util.UUID;
//
//import static org.assertj.core.api.Assertions.*;
//
//@SpringBootTest
//@Import(TestcontainersConfiguration.class)
//class SchemaMigrationTest {
//
//    private static final String CP_A = "CP-A";
//    private static final String CP_B = "CP-B";
//
//    @Autowired
//    private JdbcTemplate jdbc;
//
//    @BeforeEach
//    void reset() {
//        jdbc.execute("TRUNCATE reading, session RESTART IDENTITY");
//    }
//
//    // -- migration applied ---------------------------------------------------
//
//    @Test
//    void v2IsApplied() {
//        Integer applied = jdbc.queryForObject("""
//                SELECT count(*) FROM flyway_schema_history
//                WHERE version = '2' AND success
//                """, Integer.class);
//
//        assertThat(applied).isEqualTo(1);
//    }
//
//    /**
//     * The index must be PARTIAL. A full unique index on (charge_point_id, connector_id) would
//     * permit one session per connector ever, and would still pass every insert test that only
//     * uses one session per connector.
//     */
//    @Test
//    void activeSessionIndexIsPartial() {
//        String definition = jdbc.queryForObject("""
//                SELECT indexdef FROM pg_indexes
//                WHERE indexname = 'session_one_active_per_connector_idx'
//                """, String.class);
//
//        assertThat(definition)
//                .contains("UNIQUE")
//                .contains("WHERE (state = 'ACTIVE'::text)");
//    }
//
//    // -- natural key ---------------------------------------------------------
//
//    @Test
//    void naturalKeyIsUniquePerChargePoint() {
//        insertActiveSession(CP_A, 1000, 1);
//
//        assertThatThrownBy(() -> insertActiveSession(CP_A, 1000, 2))
//                .isInstanceOf(DataIntegrityViolationException.class);
//    }
//
//    @Test
//    void sameTransactionIdOnDifferentChargePointsIsAllowed() {
//        insertActiveSession(CP_A, 1000, 1);
//
//        assertThatCode(() -> insertActiveSession(CP_B, 1000, 1))
//                .doesNotThrowAnyException();
//    }
//
//    // -- one active session per connector ------------------------------------
//
//    @Test
//    void secondActiveSessionOnSameConnectorIsRejected() {
//        insertActiveSession(CP_A, 1000, 1);
//
//        assertThatThrownBy(() -> insertActiveSession(CP_A, 1001, 1))
//                .isInstanceOf(DataIntegrityViolationException.class);
//    }
//
//    /**
//     * The case that catches a mis-keyed index. Two connectors on one station charging
//     * simultaneously is normal operation. An index keyed on connector_id alone would reject this
//     * and every single-connector test would still pass.
//     */
//    @Test
//    void concurrentSessionsOnDifferentConnectorsOfSameChargePointAreAllowed() {
//        insertActiveSession(CP_A, 1000, 1);
//
//        assertThatCode(() -> insertActiveSession(CP_A, 1001, 2))
//                .doesNotThrowAnyException();
//    }
//
//    /**
//     * The index must self-evict on close, or a connector is blocked forever by its first session.
//     */
//    @Test
//    void closedSessionFreesTheConnector() {
//        insertActiveSession(CP_A, 1000, 1);
//        stopSession(CP_A, 1000);
//
//        assertThatCode(() -> insertActiveSession(CP_A, 1001, 1))
//                .doesNotThrowAnyException();
//    }
//
//    // -- idempotency keys ----------------------------------------------------
//
//    @Test
//    void duplicateStartMessageIdOnSameChargePointIsRejected() {
//        UUID messageId = UUID.randomUUID();
//        insertActiveSession(CP_A, 1000, 1, messageId);
//
//        assertThatThrownBy(() -> insertActiveSession(CP_A, 1001, 2, messageId))
//                .isInstanceOf(DataIntegrityViolationException.class);
//    }
//
//    @Test
//    void sameStartMessageIdOnDifferentChargePointsIsAllowed() {
//        UUID messageId = UUID.randomUUID();
//        insertActiveSession(CP_A, 1000, 1, messageId);
//
//        assertThatCode(() -> insertActiveSession(CP_B, 1000, 1, messageId))
//                .doesNotThrowAnyException();
//    }
//
//    /**
//     * stop_message_id is NULL on every active session. A non-partial unique index would reject the
//     * second active session outright.
//     */
//    @Test
//    void multipleActiveSessionsWithNullStopMessageIdAreAllowed() {
//        insertActiveSession(CP_A, 1000, 1);
//
//        assertThatCode(() -> insertActiveSession(CP_A, 1001, 2))
//                .doesNotThrowAnyException();
//    }
//
//    // -- stop field consistency ----------------------------------------------
//
//    @Test
//    void stoppedSessionWithoutMeterStopIsRejected() {
//        assertThatThrownBy(() -> jdbc.update("""
//                INSERT INTO session (
//                    charge_point_id, transaction_id, connector_id,
//                    start_id_tag, meter_start, state, start_message_id,
//                    started_occurred_at, started_received_at,
//                    stopped_occurred_at, stopped_received_at, stop_message_id
//                ) VALUES (?, ?, ?, ?, ?, 'STOPPED', ?, ?, ?, ?, ?, ?)
//                """,
//                CP_A, 1000, 1, "TOKEN-1", "1000000",
//                UUID.randomUUID(),
//                OffsetDateTime.now(), OffsetDateTime.now(),
//                OffsetDateTime.now(), OffsetDateTime.now(), UUID.randomUUID()))
//                .isInstanceOf(DataIntegrityViolationException.class);
//    }
//
//    @Test
//    void activeSessionCarryingAMeterStopIsRejected() {
//        assertThatThrownBy(() -> jdbc.update("""
//                INSERT INTO session (
//                    charge_point_id, transaction_id, connector_id,
//                    start_id_tag, meter_start, meter_stop, state, start_message_id,
//                    started_occurred_at, started_received_at
//                ) VALUES (?, ?, ?, ?, ?, ?, 'ACTIVE', ?, ?, ?)
//                """,
//                CP_A, 1000, 1, "TOKEN-1", "1000000", "1005000",
//                UUID.randomUUID(),
//                OffsetDateTime.now(), OffsetDateTime.now()))
//                .isInstanceOf(DataIntegrityViolationException.class);
//    }
//
//    // -- reading deduplication -----------------------------------------------
//
//    @Test
//    void duplicateReadingIsSwallowedByOnConflict() {
//        OffsetDateTime occurredAt = OffsetDateTime.now();
//
//        int first = insertReading(CP_A, 1000, occurredAt, "1000500", "METER_VALUES");
//        int second = insertReading(CP_A, 1000, occurredAt, "1000500", "METER_VALUES");
//
//        assertThat(first).isEqualTo(1);
//        assertThat(second)
//                .as("a conflicting insert must report zero rows so the handler can suppress "
//                    + "the outbox event")
//                .isZero();
//        assertThat(readingCount()).isEqualTo(1);
//    }
//
//    /**
//     * source is deliberately outside the dedup key: a transaction_data re-delivery of a reading
//     * already received live must collide, not insert a second row.
//     */
//    @Test
//    void transactionDataRedeliveryOfAKnownReadingCollides() {
//        OffsetDateTime occurredAt = OffsetDateTime.now();
//
//        insertReading(CP_A, 1000, occurredAt, "1000500", "METER_VALUES");
//        int redelivered = insertReading(CP_A, 1000, occurredAt, "1000500", "TRANSACTION_DATA");
//
//        assertThat(redelivered).isZero();
//        assertThat(readingCount()).isEqualTo(1);
//    }
//
//    /**
//     * Same device timestamp, different session. Must not collide.
//     */
//    @Test
//    void sameTimestampOnDifferentTransactionsIsAllowed() {
//        OffsetDateTime occurredAt = OffsetDateTime.now();
//
//        insertReading(CP_A, 1000, occurredAt, "1000500", "METER_VALUES");
//        insertReading(CP_A, 1001, occurredAt, "2000500", "METER_VALUES");
//
//        assertThat(readingCount()).isEqualTo(2);
//    }
//
//    // -- raw storage ---------------------------------------------------------
//
//    /**
//     * The producer stores what arrived. A non-numeric register value (P-12) must persist, not be
//     * rejected at the write boundary — discarding it makes the cleaning logic unrevisable.
//     */
//    @Test
//    void nonNumericReadingValueIsStored() {
//        assertThatCode(() ->
//                insertReading(CP_A, 1000, OffsetDateTime.now(), "NaN", "METER_VALUES"))
//                .doesNotThrowAnyException();
//
//        assertThat(readingCount()).isEqualTo(1);
//    }
//
//    /**
//     * A reading whose StartTransaction was permanently discarded (P-16) is real data. No foreign
//     * key may reject it.
//     */
//    @Test
//    void orphanReadingWithNoSessionIsAccepted() {
//        assertThatCode(() ->
//                insertReading(CP_A, 9999, OffsetDateTime.now(), "1000500", "METER_VALUES"))
//                .doesNotThrowAnyException();
//
//        assertThat(readingCount()).isEqualTo(1);
//    }
//
//    /**
//     * Absent measurand/unit/context must be distinguishable from present. NULL is the signal;
//     * defaulting happens in the warehouse.
//     */
//    @Test
//    void absentOptionalFieldsPersistAsNull() {
//        insertReading(CP_A, 1000, OffsetDateTime.now(), "1000500", "METER_VALUES");
//
//        Integer nulls = jdbc.queryForObject("""
//                SELECT count(*) FROM reading
//                WHERE measurand IS NULL AND unit IS NULL AND context IS NULL
//                """, Integer.class);
//
//        assertThat(nulls).isEqualTo(1);
//    }
//
//    // -- helpers -------------------------------------------------------------
//
//    private void insertActiveSession(String chargePointId, int transactionId, int connectorId) {
//        insertActiveSession(chargePointId, transactionId, connectorId, UUID.randomUUID());
//    }
//
//    private void insertActiveSession(String chargePointId, int transactionId, int connectorId,
//                                     UUID startMessageId) {
//        jdbc.update("""
//                INSERT INTO session (
//                    charge_point_id, transaction_id, connector_id,
//                    start_id_tag, meter_start, state, start_message_id,
//                    started_occurred_at, started_received_at
//                ) VALUES (?, ?, ?, ?, ?, 'ACTIVE', ?, ?, ?)
//                """,
//                chargePointId, transactionId, connectorId,
//                "TOKEN-1", "1000000", startMessageId,
//                OffsetDateTime.now(), OffsetDateTime.now());
//    }
//
//    private void stopSession(String chargePointId, int transactionId) {
//        jdbc.update("""
//                UPDATE session SET
//                    state = 'STOPPED',
//                    meter_stop = ?,
//                    stopped_occurred_at = ?,
//                    stopped_received_at = ?,
//                    stop_message_id = ?
//                WHERE charge_point_id = ? AND transaction_id = ?
//                """,
//                "1005000", OffsetDateTime.now(), OffsetDateTime.now(), UUID.randomUUID(),
//                chargePointId, transactionId);
//    }
//
//    private int insertReading(String chargePointId, int transactionId, OffsetDateTime occurredAt,
//                              String value, String source) {
//        return jdbc.update("""
//                INSERT INTO reading (
//                    charge_point_id, transaction_id, occurred_at, received_at,
//                    value, source, message_id
//                ) VALUES (?, ?, ?, ?, ?, ?, ?)
//                ON CONFLICT (charge_point_id, transaction_id, occurred_at) DO NOTHING
//                """,
//                chargePointId, transactionId, occurredAt, OffsetDateTime.now(),
//                value, source, UUID.randomUUID());
//    }
//
//    private int readingCount() {
//        return jdbc.queryForObject("SELECT count(*) FROM reading", Integer.class);
//    }
//}
