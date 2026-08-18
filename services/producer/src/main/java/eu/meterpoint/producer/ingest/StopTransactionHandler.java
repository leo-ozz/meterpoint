package eu.meterpoint.producer.ingest;

import java.time.Instant;
import java.util.UUID;

import eu.meterpoint.producer.api.payloads.StopTransactionPayload;
import eu.meterpoint.producer.domain.outbox.OutboxRepository;
import eu.meterpoint.producer.domain.session.SessionRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class StopTransactionHandler {

    private final SessionRepository sessionRepository;
    private final OutboxRepository outboxRepository;

    public StopTransactionHandler(
            SessionRepository sessionRepository,
            OutboxRepository outboxRepository) {
        this.sessionRepository = sessionRepository;
        this.outboxRepository = outboxRepository;
    }

    @Transactional
    public void handle(
            String chargePointId,
            UUID messageId,
            StopTransactionPayload payload,
            Instant receivedAt) {
        // TODO
    }
}
