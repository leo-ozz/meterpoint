package eu.meterpoint.producer.domain.session;

import java.time.Instant;
import java.util.UUID;

public record Session(
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
        Instant stoppedReceivedAt
) {}