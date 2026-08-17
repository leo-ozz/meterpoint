package eu.meterpoint.producer.domain.outbox;

import java.time.Instant;
import java.util.UUID;

public record OutboxEvent(
        Long id,
        UUID eventId,
        String aggregateType,
        UUID aggregateId,
        EventType eventType,
        int schemaVersion,
        String payload,
        Instant occurredAt,
        Instant receivedAt
) {}
