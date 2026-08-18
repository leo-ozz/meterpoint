package eu.meterpoint.producer.api.payloads;

public record StartTransactionResponse(
        int transactionId,
        String idTagStatus
) {}