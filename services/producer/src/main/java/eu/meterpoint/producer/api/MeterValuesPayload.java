package eu.meterpoint.producer.api;

import java.util.List;

public record MeterValuesPayload(
        int transactionId,
        List<SamplePayload> samples
) {}