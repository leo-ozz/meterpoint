package eu.meterpoint.producer.api.exceptions;

public class UnknownActionException extends RuntimeException {

    public UnknownActionException(String message) {
        super(message);
    }
}
