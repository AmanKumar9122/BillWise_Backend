package com.aksps.BillWise.config;

import com.aksps.BillWise.exception.ResourceNotFoundException;
import com.aksps.BillWise.exception.ValidationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global Exception Handler to centralize error processing across all REST controllers.
 * Ensures consistent and client-friendly error responses (JSON format).
 */
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // Helper method to build a standard error response body
    private Map<String, Object> buildErrorBody(HttpStatus status, String message, String details) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        body.put("path", details);
        return body;
    }

    // 1. Handle Resource Not Found (404)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        Map<String, Object> body = buildErrorBody(
                status,
                ex.getMessage(),
                request.getDescription(false)
        );
        return new ResponseEntity<>(body, status);
    }

    // 2. Handle Business Validation Errors (400)
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Object> handleValidationException(ValidationException ex, WebRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        Map<String, Object> body = buildErrorBody(
                status,
                ex.getMessage(),
                request.getDescription(false)
        );
        return new ResponseEntity<>(body, status);
    }

    // 3. Handle JSR-380 Validation Errors (@Valid DTOs) (400)
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        // Collect all field validation errors
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());

        Map<String, Object> body = buildErrorBody(
                HttpStatus.BAD_REQUEST,
                "Validation failed for request body.",
                request.getDescription(false)
        );
        body.put("errors", errors); // Add detailed field errors

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    // 4. Handle all other uncaught exceptions (500)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGlobalException(Exception ex, WebRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        Map<String, Object> body = buildErrorBody(
                status,
                "An unexpected error occurred. Please try again later.",
                request.getDescription(false)
        );
        // Log the full exception stack trace for debugging purposes
        logger.error("Internal Server Error:", ex);
        return new ResponseEntity<>(body, status);
    }
}