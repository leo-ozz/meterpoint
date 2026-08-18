package eu.meterpoint.producer.domain.session;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class SessionRepository {

    private static final SessionRowMapper ROW_MAPPER = new SessionRowMapper();
    private static final String COLUMNS = """
        id,
        charge_point_id,
        transaction_id,
        connector_id,
        start_id_tag,
        stop_id_tag,
        meter_start,
        meter_stop,
        stop_reason,
        state,
        start_message_id,
        stop_message_id,
        started_occurred_at,
        started_received_at,
        stopped_occurred_at,
        stopped_received_at,
        created_at,
        updated_at
        """;

    private final JdbcClient jdbcClient;

    public SessionRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public Optional<Session> findActiveForUpdate(String chargePointId, int connectorId) {
        return jdbcClient.sql("""
            SELECT
            """ + COLUMNS + """
            FROM session
            WHERE charge_point_id = :chargePointId
                AND connector_id = :connectorId
                AND state = :state
            FOR UPDATE
            """)
                .param("chargePointId", chargePointId)
                .param("connectorId", connectorId)
                .param("state", SessionState.ACTIVE.name())
                .query(ROW_MAPPER)
                .optional();
    }

    public void supersede(Long sessionId, String reason, Instant updatedAt) {
        int count =  jdbcClient.sql("""
            UPDATE session
            SET state = :newState,
               stop_reason = :stopReason,
               updated_at = :updatedAt
            WHERE id = :sessionId
            """)
                .param("newState", "SUPERSEDED")
                .param("stopReason", reason)
                .param("updatedAt", updatedAt)
                .param("sessionId", sessionId)
                .update();

        if (count != 1) {
            throw new IllegalStateException("Could not find entry to supersede for: sessionId="+sessionId);
        }
    }

    public Session insertActive(Session session) {
        return jdbcClient.sql("""
        INSERT INTO session (
            charge_point_id,
            connector_id,
            start_id_tag,
            meter_start,
            state,
            start_message_id,
            started_occurred_at,
            started_received_at
        )
        VALUES (
            :chargePointId,
            :connectorId,
            :startIdTag,
            :meterStart,
            :state,
            :startMessageId,
            :startedOccurredAt,
            :startedReceivedAt
        )
        RETURNING
        """ + COLUMNS)
                .param("chargePointId", session.getChargePointId())
                .param("connectorId", session.getConnectorId())
                .param("startIdTag", session.getStartIdTag())
                .param("meterStart", session.getMeterStart())
                .param("state", SessionState.ACTIVE.name())
                .param("startMessageId", session.getStartMessageId())
                .param("startedOccurredAt", Timestamp.from(session.getStartedOccurredAt()))
                .param("startedReceivedAt", Timestamp.from(session.getStartedReceivedAt()))
                .query(ROW_MAPPER)
                .single();
    }
    public Optional<Session> findByNaturalKeyForUpdate(String chargePointId, int transactionId) {
        return jdbcClient.sql("""
            SELECT
            """ + COLUMNS + """
            FROM session
            WHERE charge_point_id = :chargePointId
                AND transaction_id = :transactionId
            FOR UPDATE
            """)
                .param("chargePointId", chargePointId)
                .param("transactionId", transactionId)
                .query(ROW_MAPPER)
                .optional();
    }
}