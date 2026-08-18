package eu.meterpoint.producer.domain.inbox;

import eu.meterpoint.producer.api.Action;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class InboxRepository {

    private final JdbcClient jdbcClient;

    public InboxRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public boolean tryInsert(String chargePointId, UUID messageId, Action action, byte[] payloadHash) {
        int count = jdbcClient.sql("""
            INSERT INTO inbox (
                charge_point_id,
                message_id,
                action,
                payload_hash
            )
            VALUES (:chargePointId, :messageId, :action, :payloadHash)
            ON CONFLICT (charge_point_id, message_id) DO NOTHING
            """)
                .param("chargePointId", chargePointId)
                .param("messageId", messageId)
                .param("action", action.wireValue())
                .param("payloadHash", payloadHash)
                .update();

        return count == 1;
    }

    public Optional<String> findResponse(String chargePointId, UUID messageId) {
        return jdbcClient.sql("""
            SELECT response_body FROM inbox
            WHERE charge_point_id = :chargePointId AND message_id = :messageId
            """)
                .param("chargePointId", chargePointId)
                .param("messageId", messageId)
                .query(String.class)
                .optional();
    }

    public void storeResponse(String chargePointId, UUID messageId, String responseBody) {
        int count = jdbcClient.sql("""
            UPDATE inbox SET response_body = :responseBody::jsonb
            WHERE charge_point_id = :chargePointId AND message_id = :messageId
            """)
                .param("chargePointId", chargePointId)
                .param("messageId", messageId)
                .param("responseBody", responseBody)
                .update();

        if (count != 1) {
            throw new IllegalStateException(
                    "Inbox row vanished between claim and store: " + chargePointId + "/" + messageId);
        }
    }
}
