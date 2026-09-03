package com.netgsm.asterisk.exception;

import com.netgsm.asterisk.response.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.method.MethodValidationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

@Slf4j @RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(PlatformException.class)
    ResponseEntity<ApiError> platform(PlatformException ex, HttpServletRequest request) {
        return error(ex.status(), ex.code(), ex.getMessage(), request, Map.of());
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> validation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(e -> errors.putIfAbsent(e.getField(), e.getDefaultMessage()));
        return error(400, "VALIDATION_ERROR", "Validation failed", request, errors);
    }
    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            ConstraintViolationException.class, MethodValidationException.class, IllegalArgumentException.class})
    ResponseEntity<ApiError> invalid(Exception ex, HttpServletRequest request) {
        return error(400, "BAD_REQUEST", "Invalid request", request, Map.of());
    }
    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ApiError> forbidden(AccessDeniedException ex, HttpServletRequest request) {
        return error(403, "FORBIDDEN", "Access denied", request, Map.of());
    }
    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ApiError> conflict(DataIntegrityViolationException ex, HttpServletRequest request) {
        return error(409, "CONFLICT", "Duplicate record or referenced resource", request, Map.of());
    }
    @ExceptionHandler(org.springframework.orm.ObjectOptimisticLockingFailureException.class)
    ResponseEntity<ApiError> concurrent(Exception ex, HttpServletRequest request) {
        return error(409, "CONFLICT", "Record changed; reload and retry", request, Map.of());
    }
    @ExceptionHandler(DataAccessException.class)
    ResponseEntity<ApiError> database(DataAccessException ex, HttpServletRequest request) {
        log.error("Database operation failed ({})", ex.getClass().getSimpleName());
        return error(500, "DATABASE_ERROR", "Database operation failed", request, Map.of());
    }
    @ExceptionHandler(org.springframework.web.ErrorResponseException.class)
    ResponseEntity<ApiError> http(org.springframework.web.ErrorResponseException ex, HttpServletRequest request) {
        return error(ex.getStatusCode().value(), "REQUEST_ERROR", "Request could not be processed", request, Map.of());
    }
    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiError> unexpected(Exception ex, HttpServletRequest request) {
        log.error("Request failed ({})", ex.getClass().getSimpleName());
        return error(500, "INTERNAL_ERROR", "Unexpected server error", request, Map.of());
    }
    private ResponseEntity<ApiError> error(int status, String code, String message,
            HttpServletRequest request, Map<String, String> errors) {
        return ResponseEntity.status(status).body(new ApiError(Instant.now(), status, code, message,
                request.getRequestURI(), errors));
    }
}
