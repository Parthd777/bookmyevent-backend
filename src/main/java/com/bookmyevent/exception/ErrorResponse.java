package com.bookmyevent.exception;

import java.time.LocalDateTime;

/**
 * Structured error body returned by {@link GlobalExceptionHandler}.
 *
 * <p>Day 4 mentoring point: always return a consistent error schema from a REST API.
 * Exposing raw exception stack traces is a security risk and a poor client experience.
 * The client always sees this shape regardless of which exception was thrown internally.
 */
public class ErrorResponse {

    private int status;
    private String error;
    private String message;
    private LocalDateTime timestamp;

    public ErrorResponse(int status, String error, String message) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    public int getStatus() { return status; }
    public String getError() { return error; }
    public String getMessage() { return message; }
    public LocalDateTime getTimestamp() { return timestamp; }
}
