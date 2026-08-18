package eu.meterpoint.producer.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import eu.meterpoint.producer.api.exceptions.UnknownActionException;

public enum Action {
    START_TRANSACTION("StartTransaction"),
    METER_VALUES("MeterValues"),
    STOP_TRANSACTION("StopTransaction");

    private final String wireValue;

    Action(String wireValue) {
        this.wireValue = wireValue;
    }

    @JsonValue
    public String wireValue() {
        return wireValue;
    }

    @JsonCreator
    public static Action fromWire(String value) {
        for (Action a : values()) {
            if (a.wireValue.equals(value)) {
                return a;
            }
        }
        throw new UnknownActionException("Unknown action: " + value);
    }
}