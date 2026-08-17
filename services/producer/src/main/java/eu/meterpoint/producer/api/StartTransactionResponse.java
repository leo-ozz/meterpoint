package eu.meterpoint.producer.api;

public record StartTransactionResponse(
        int transactionId,
        String idTagStatus
) {}