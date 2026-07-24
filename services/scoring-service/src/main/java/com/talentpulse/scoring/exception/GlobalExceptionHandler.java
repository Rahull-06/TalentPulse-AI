package com.talentpulse.scoring.exception;

import com.talentpulse.scoring.dto.ApiErrorResponse;
import java.time.Instant;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<ApiErrorResponse.FieldErrorItem> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::toFieldError)
                .toList();
        return build(HttpStatus.BAD_REQUEST, "Validation failed", fieldErrors);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), List.of());
    }

    @ExceptionHandler({ForbiddenActionException.class, AccessDeniedException.class})
    public ResponseEntity<ApiErrorResponse> handleForbidden(RuntimeException ex) {
        return build(
                HttpStatus.FORBIDDEN,
                ex.getMessage() != null ? ex.getMessage() : "Access denied",
                List.of()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(Exception ex) {
        log.error("Unexpected error", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error", List.of());
    }

    private ApiErrorResponse.FieldErrorItem toFieldError(FieldError error) {
        return new ApiErrorResponse.FieldErrorItem(
                error.getField(),
                error.getDefaultMessage() != null ? error.getDefaultMessage() : "Invalid value"
        );
    }

    private ResponseEntity<ApiErrorResponse> build(
            HttpStatus status,
            String message,
            List<ApiErrorResponse.FieldErrorItem> errors
    ) {
        return ResponseEntity.status(status).body(
                ApiErrorResponse.builder()
                        .success(false)
                        .message(message)
                        .errors(errors)
                        .timestamp(Instant.now())
                        .build()
        );
    }
}
