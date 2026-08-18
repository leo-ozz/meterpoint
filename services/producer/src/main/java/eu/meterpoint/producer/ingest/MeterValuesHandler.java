package eu.meterpoint.producer.ingest;

import java.time.Instant;
import java.util.UUID;

import eu.meterpoint.producer.api.payloads.MeterValuesPayload;
import eu.meterpoint.producer.domain.outbox.OutboxRepository;
import eu.meterpoint.producer.domain.reading.ReadingRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class MeterValuesHandler {

    private final ReadingRepository readingRepository;
    private final OutboxRepository outboxRepository;

    public MeterValuesHandler(
            ReadingRepository readingRepository,
            OutboxRepository outboxRepository) {
        this.readingRepository = readingRepository;
        this.outboxRepository = outboxRepository;
    }

    @Transactional
    public void handle(
            String chargePointId,
            UUID messageId,
            MeterValuesPayload payload,
            Instant receivedAt) {
        // TODO
    }
}
