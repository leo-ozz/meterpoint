package eu.meterpoint.producer.api.payloads;

import java.time.OffsetDateTime;
import java.util.List;

public record StopTransactionPayload(
        int transactionId,
        String meterStop,
        OffsetDateTime timestamp,
        String idTag,
        String reason,
        List<SamplePayload> transactionData
) {}