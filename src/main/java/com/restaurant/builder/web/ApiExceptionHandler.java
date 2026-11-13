package com.restaurant.builder.web;

import com.restaurant.builder.application.OrderNotFoundException;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public final class ApiExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<Map<String, String>> invalid(IllegalArgumentException exception) { return error(400, exception.getMessage()); }
    @ExceptionHandler(IllegalStateException.class)
    ResponseEntity<Map<String, String>> conflict(IllegalStateException exception) { return error(409, exception.getMessage()); }
    @ExceptionHandler(OrderNotFoundException.class)
    ResponseEntity<Map<String, String>> missing(OrderNotFoundException exception) { return error(404, exception.getMessage()); }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String, String>> validation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
            .map(field -> field.getField() + ": " + field.getDefaultMessage()).sorted().findFirst().orElse("Invalid order details");
        return error(400, message);
    }
    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    ResponseEntity<Map<String, String>> unreadable(Exception exception) {
        return error(400, "Invalid request: check JSON, UUIDs, quantities, and fulfillment (DINE_IN or TAKEAWAY)");
    }
    @ExceptionHandler(org.springframework.web.HttpMediaTypeNotSupportedException.class)
    ResponseEntity<Map<String, String>> mediaType(Exception exception) { return error(415, "Use Content-Type: application/json"); }
    private ResponseEntity<Map<String, String>> error(int status, String message) {
        return ResponseEntity.status(status).body(Map.of("message", message == null ? "Invalid request" : message));
    }
}
