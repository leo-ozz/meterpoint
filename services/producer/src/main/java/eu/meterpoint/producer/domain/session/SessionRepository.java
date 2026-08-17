package eu.meterpoint.producer.domain.session;

import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class SessionRepository {

    private final JdbcClient jdbcClient;

    public SessionRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public Optional<Session> findByStartMessageId(
            String chargePointId,
            UUID startMessageId) {
        // TODO
        return Optional.empty();
    }

    public Optional<Session> findByNaturalKeyForUpdate(
            String chargePointId,
            int transactionId) {
        // TODO
        return Optional.empty();
    }

    public int insert(Session session) {
        // TODO
        return 0;
    }

    public int applyStop(Session session) {
        // TODO
        return 0;
    }
}