package com.example.bankcards.exception;

import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.security.access.AccessDeniedException;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Единый формат ответа для ошибок
    public record ErrorResponse(String message) {}

    @PostConstruct
    public void init() {
        logger.info("GlobalExceptionHandler loaded");
    }

    @ExceptionHandler(CardException.class)
    public ResponseEntity<ErrorResponse> handleCardException(CardException e) {
        logger.warn("Card error: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(UserException.class)
    public ResponseEntity<ErrorResponse> handleUserException(UserException e) {
        logger.warn("User error: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials() {
        String msg = "Invalid username or password";
        logger.warn(msg);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(msg));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException e) {
        logger.warn("Authentication failed", e);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse("Authentication failed"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        logger.warn("Validation failed: {}", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("Validation failed: " + errors));
    }

    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<ErrorResponse> handleSortProperty(PropertyReferenceException e) {
        String msg = "Invalid sort parameter: property '" + e.getPropertyName() + "' not found";
        logger.warn(msg);
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(msg));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        String name = e.getName();
        String value = e.getValue() != null ? e.getValue().toString() : "null";
        String msg = "Invalid value '" + value + "' for parameter '" + name + "'. Must be a valid number.";
        logger.warn(msg);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(msg));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception e) {
        logger.error("Unexpected error", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Internal server error"));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied() {
        String msg = "Доступ запрещён";
        logger.warn(msg);
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse(msg));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFound() {
        String msg = "Объект не найден";
        logger.warn(msg);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(msg));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        String msg;
        if (e.getMessage().contains("card_number")) {
            msg = "Номер карты уже существует";
        } else {
            msg = "Ошибка целостности данных";
        }
        logger.warn("Data integrity violation: {}", msg);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(msg));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(ResponseStatusException e) {
        return ResponseEntity.status(e.getStatusCode())
                .body(new ErrorResponse(e.getReason()));
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLock() {
        String msg = "Данные устарели. Обновите и попробуйте снова.";
        logger.warn(msg);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(msg));
    }
}