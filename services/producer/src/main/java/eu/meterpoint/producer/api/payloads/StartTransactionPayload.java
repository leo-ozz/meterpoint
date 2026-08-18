package eu.meterpoint.producer.api.payloads;

import java.time.OffsetDateTime;

public record StartTransactionPayload(
        int connectorId,
        String idTag,
        String meterStart,
        OffsetDateTime timestamp
) {}