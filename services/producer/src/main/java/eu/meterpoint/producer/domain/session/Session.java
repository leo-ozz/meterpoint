package eu.meterpoint.producer.domain.session;

import eu.meterpoint.producer.domain.session.SessionState;

import java.time.Instant;
import java.util.UUID;

public class Session {

    private Long id;
    private String chargePointId;
    private int transactionId;
    private int connectorId;
    private String startIdTag;
    private String stopIdTag;
    private String meterStart;
    private String meterStop;
    private String stopReason;
    private SessionState state;
    private UUID startMessageId;
    private UUID stopMessageId;
    private Instant startedOccurredAt;
    private Instant startedReceivedAt;
    private Instant stoppedOccurredAt;
    private Instant stoppedReceivedAt;
    private Instant createdAt;
    private Instant updatedAt;

    public Session(
            Long id,
            String chargePointId,
            int transactionId,
            int connectorId,
            String startIdTag,
            String stopIdTag,
            String meterStart,
            String meterStop,
            String stopReason,
            SessionState state,
            UUID startMessageId,
            UUID stopMessageId,
            Instant startedOccurredAt,
            Instant startedReceivedAt,
            Instant stoppedOccurredAt,
            Instant stoppedReceivedAt,
            Instant createdAt,
            Instant updatedAt) {
        this.id = id;
        this.chargePointId = chargePointId;
        this.transactionId = transactionId;
        this.connectorId = connectorId;
        this.startIdTag = startIdTag;
        this.stopIdTag = stopIdTag;
        this.meterStart = meterStart;
        this.meterStop = meterStop;
        this.stopReason = stopReason;
        this.state = state;
        this.startMessageId = startMessageId;
        this.stopMessageId = stopMessageId;
        this.startedOccurredAt = startedOccurredAt;
        this.startedReceivedAt = startedReceivedAt;
        this.stoppedOccurredAt = stoppedOccurredAt;
        this.stoppedReceivedAt = stoppedReceivedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public String getChargePointId() {
        return chargePointId;
    }

    public int getTransactionId() {
        return transactionId;
    }

    public int getConnectorId() {
        return connectorId;
    }

    public String getStartIdTag() {
        return startIdTag;
    }

    public String getStopIdTag() {
        return stopIdTag;
    }

    public String getMeterStart() {
        return meterStart;
    }

    public String getMeterStop() {
        return meterStop;
    }

    public String getStopReason() {
        return stopReason;
    }

    public SessionState getState() {
        return state;
    }

    public UUID getStartMessageId() {
        return startMessageId;
    }

    public UUID getStopMessageId() {
        return stopMessageId;
    }

    public Instant getStartedOccurredAt() {
        return startedOccurredAt;
    }

    public Instant getStartedReceivedAt() {
        return startedReceivedAt;
    }

    public Instant getStoppedOccurredAt() {
        return stoppedOccurredAt;
    }

    public Instant getStoppedReceivedAt() {
        return stoppedReceivedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}