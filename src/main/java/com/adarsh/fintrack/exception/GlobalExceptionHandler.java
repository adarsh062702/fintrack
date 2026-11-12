package com.adarsh.fintrack.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private Map<String, Object> createErrorBody(
            int status,
            String error,
            String message) {

        Map<String, Object> body = new LinkedHashMap<>();

        body.put("timestamp", LocalDateTime.now());
        body.put("status", status);
        body.put("error", error);
        body.put("message", message);

        return body;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFound(
            ResourceNotFoundException exception) {

        HttpStatus status = HttpStatus.NOT_FOUND;

        return ResponseEntity.status(status).body(
                createErrorBody(
                        status.value(),
                        status.getReasonPhrase(),
                        exception.getMessage()
                )
        );
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatus(
            ResponseStatusException exception) {

        HttpStatusCode status = exception.getStatusCode();

        String message = exception.getReason() != null
                ? exception.getReason()
                : "Request could not be completed";

        return ResponseEntity.status(status).body(
                createErrorBody(
                        status.value(),
                        status.toString(),
                        message
                )
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(
            AccessDeniedException exception) {

        HttpStatus status = HttpStatus.FORBIDDEN;

        return ResponseEntity.status(status).body(
                createErrorBody(
                        status.value(),
                        status.getReasonPhrase(),
                        "You do not have permission to perform this action."
                )
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(
            MethodArgumentNotValidException exception) {

        HttpStatus status = HttpStatus.BAD_REQUEST;

        Map<String, String> fieldErrors = new LinkedHashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        fieldErrors.put(
                                error.getField(),
                                error.getDefaultMessage()
                        )
                );

        Map<String, Object> body = createErrorBody(
                status.value(),
                status.getReasonPhrase(),
                "One or more request fields are invalid."
        );

        body.put("fieldErrors", fieldErrors);

        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidRequestBody(
            HttpMessageNotReadableException exception) {

        HttpStatus status = HttpStatus.BAD_REQUEST;

        return ResponseEntity.status(status).body(
                createErrorBody(
                        status.value(),
                        status.getReasonPhrase(),
                        "Invalid request body. Check your JSON format and date values."
                )
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(
            IllegalArgumentException exception) {

        HttpStatus status = HttpStatus.BAD_REQUEST;

        return ResponseEntity.status(status).body(
                createErrorBody(
                        status.value(),
                        status.getReasonPhrase(),
                        exception.getMessage() != null
                                ? exception.getMessage()
                                : "Invalid request"
                )
        );
    }
}