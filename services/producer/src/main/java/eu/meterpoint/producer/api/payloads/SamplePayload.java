package eu.meterpoint.producer.api.payloads;

import java.time.OffsetDateTime;

public record SamplePayload(
        OffsetDateTime timestamp,
        String value,
        String measurand,
        String unit,
        String context
) {}
