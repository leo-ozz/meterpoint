package eu.meterpoint.producer.domain.reading;

import java.time.Instant;
import java.util.UUID;

public record Reading(
        Long id,
        String chargePointId,
        int transactionId,
        Instant occurredAt,
        Instant receivedAt,
        String value,
        String measurand,
        String unit,
        String context,
        ReadingSource source,
        UUID messageId
) {}