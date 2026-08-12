package com.bookmyevent.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * Day 4 — Centralised exception handling.
 *
 * <p>Without this class, Spring Boot would return its own default error JSON
 * (from {@code /error} endpoint) which is inconsistent and may leak internals.
 *
 * <p>{@code @RestControllerAdvice} = {@code @ControllerAdvice} + {@code @ResponseBody}.
 * Every exception thrown from any {@code @RestController} is intercepted here
 * before reaching the client — one place to shape all error responses.
 *
 * <p>Mapping:
 * <pre>
 *   UserNotFoundException        → 404 Not Found
 *   BookingNotAllowedException   → 400 Bad Request
 *   EventFullException           → 409 Conflict
 *   MethodArgumentNotValidException → 400 Bad Request (validation failures)
 *   Exception (fallback)         → 500 Internal Server Error
 * </pre>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(BookingNotAllowedException.class)
    public ResponseEntity<ErrorResponse> handleBookingNotAllowed(BookingNotAllowedException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(EventFullException.class)
    public ResponseEntity<ErrorResponse> handleEventFull(EventFullException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage());
    }

    /**
     * Handles @Valid / @Validated failures.
     * Collects all field-level constraint violations into a single message.
     *
     * <p>Example response body:
     * <pre>
     * {
     *   "status": 400,
     *   "error": "Validation Failed",
     *   "message": "email: must be a well-formed email address; name: must not be blank"
     * }
     * </pre>
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return build(HttpStatus.BAD_REQUEST, details);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        // Do NOT expose ex.getMessage() to the client in production — it may leak internals.
        // Log it server-side instead (Day 7 adds structured logging).
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.");
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(new ErrorResponse(status.value(), status.getReasonPhrase(), message));
    }
}
