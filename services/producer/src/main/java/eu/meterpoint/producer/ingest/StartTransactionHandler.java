package eu.meterpoint.producer.ingest;

import java.time.Instant;
import java.util.UUID;

import eu.meterpoint.producer.api.StartTransactionPayload;
import eu.meterpoint.producer.api.StartTransactionResponse;
import eu.meterpoint.producer.domain.outbox.OutboxRepository;
import eu.meterpoint.producer.domain.session.SessionRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class StartTransactionHandler {

    private final SessionRepository sessionRepository;
    private final OutboxRepository outboxRepository;

    public StartTransactionHandler(
            SessionRepository sessionRepository,
            OutboxRepository outboxRepository) {
        this.sessionRepository = sessionRepository;
        this.outboxRepository = outboxRepository;
    }

    @Transactional
    public StartTransactionResponse handle(
            String chargePointId,
            UUID messageId,
            StartTransactionPayload payload,
            Instant receivedAt) {
        // TODO
        return null;
    }
}