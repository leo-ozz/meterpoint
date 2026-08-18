package eu.meterpoint.producer.api.payloads;

import java.util.List;

public record MeterValuesPayload(
        int transactionId,
        List<SamplePayload> samples
) {}