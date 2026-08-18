package eu.meterpoint.producer.api.exceptions;

public class UnknownTransactionException extends RuntimeException {

    public UnknownTransactionException(String message) {
        super(message);
    }
}
