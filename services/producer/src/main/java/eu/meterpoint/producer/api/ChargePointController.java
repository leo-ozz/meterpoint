package eu.meterpoint.producer.api;

import java.time.Clock;
import java.time.Instant;

import eu.meterpoint.producer.ingest.MeterValuesHandler;
import eu.meterpoint.producer.ingest.StartTransactionHandler;
import eu.meterpoint.producer.ingest.StopTransactionHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChargePointController {

    private final Clock clock;
    //private final ObjectMapper objectMapper;
    private final StartTransactionHandler startTransactionHandler;
    private final MeterValuesHandler meterValuesHandler;
    private final StopTransactionHandler stopTransactionHandler;

    public ChargePointController(
            Clock clock,
            //ObjectMapper objectMapper,
            StartTransactionHandler startTransactionHandler,
            MeterValuesHandler meterValuesHandler,
            StopTransactionHandler stopTransactionHandler) {
        this.clock = clock;
        //this.objectMapper = objectMapper;
        this.startTransactionHandler = startTransactionHandler;
        this.meterValuesHandler = meterValuesHandler;
        this.stopTransactionHandler = stopTransactionHandler;
    }

//    @PostMapping("/cp/{chargePointId}")
//    public ResponseEntity<?> handle(
//            @PathVariable String chargePointId,
//            @RequestBody Envelope envelope) {
//
//        Instant receivedAt = clock.instant();
//
//        return switch (envelope.action()) {
//            case "StartTransaction" -> {
//                StartTransactionPayload payload =
//                        objectMapper.convertValue(
//                                envelope.payload(),
//                                StartTransactionPayload.class);
//
//                StartTransactionResponse response =
//                        startTransactionHandler.handle(
//                                chargePointId,
//                                envelope.messageId(),
//                                payload,
//                                receivedAt);
//
//                yield ResponseEntity.status(201).body(response);
//            }
//
//            case "MeterValues" -> {
//                MeterValuesPayload payload =
//                        objectMapper.convertValue(
//                                envelope.payload(),
//                                MeterValuesPayload.class);
//
//                meterValuesHandler.handle(
//                        chargePointId,
//                        envelope.messageId(),
//                        payload,
//                        receivedAt);
//
//                yield ResponseEntity.accepted().build();
//            }
//
//            case "StopTransaction" -> {
//                StopTransactionPayload payload =
//                        objectMapper.convertValue(
//                                envelope.payload(),
//                                StopTransactionPayload.class);
//
//                stopTransactionHandler.handle(
//                        chargePointId,
//                        envelope.messageId(),
//                        payload,
//                        receivedAt);
//
//                yield ResponseEntity.ok().build();
//            }
//
//            default -> ResponseEntity.badRequest().build();
//        };
//    }
}