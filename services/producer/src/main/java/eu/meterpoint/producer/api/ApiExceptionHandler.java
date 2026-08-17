package eu.meterpoint.producer.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    public record ErrorResponse(String error) {}

//    @ExceptionHandler({
//            DeserializationException.class,
//            UnknownActionException.class
//    })
//    public ResponseEntity<ErrorResponse> handleBadRequest(Exception exception) {
//        return ResponseEntity
//                .status(HttpStatus.BAD_REQUEST)
//                .body(new ErrorResponse(exception.getMessage()));
//    }
//
//    @ExceptionHandler(UnknownTransactionException.class)
//    public ResponseEntity<ErrorResponse> handleUnknownTransaction(
//            UnknownTransactionException exception) {
//        return ResponseEntity
//                .status(HttpStatus.NOT_FOUND)
//                .body(new ErrorResponse(exception.getMessage()));
//    }
//
//    @ExceptionHandler(StopConflictException.class)
//    public ResponseEntity<ErrorResponse> handleStopConflict(
//            StopConflictException exception) {
//        return ResponseEntity
//                .status(HttpStatus.CONFLICT)
//                .body(new ErrorResponse(exception.getMessage()));
//    }
}
