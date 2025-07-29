package com.deyan.mealplanner.controller;

import com.deyan.mealplanner.dto.*;
import com.deyan.mealplanner.exceptions.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.*;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.converter.HttpMessageNotReadableException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler({IllegalArgumentException.class, BadRequestException.class})
    public ResponseEntity<ErrorResponse> badRequest(RuntimeException ex) {
        return build(HttpStatus.BAD_REQUEST, ex);
    }

    @ExceptionHandler({
            IllegalStateException.class,
            DuplicateKeyException.class
    })
    public ResponseEntity<ErrorResponse> conflict(RuntimeException ex) {
        return build(HttpStatus.CONFLICT, ex);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> badJson(HttpMessageNotReadableException ex) {
        return build(HttpStatus.BAD_REQUEST, "Malformed request body.");
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> unauthorized(BadCredentialsException ex) {
        return build(HttpStatus.UNAUTHORIZED, ex);
    }

    @ExceptionHandler(AlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> alreadyExists(AlreadyExistsException ex) {
        return build(HttpStatus.CONFLICT, ex);
    }
    @ExceptionHandler(RefreshTokenException.class)
    public ResponseEntity<ErrorResponse> handleRefreshToken(RefreshTokenException ex) {
        return build(HttpStatus.UNAUTHORIZED, ex);  // or 400 depending on your preference
    }
    @ExceptionHandler(ExternalApiQuotaException.class)
    public ResponseEntity<ErrorResponse> tooMany(ExternalApiQuotaException ex) {
        return build(HttpStatus.TOO_MANY_REQUESTS, ex);
    }
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> notFound(NotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex);
    }

    // Bean-validation (@Valid) errors → 400 with concatenated field messages
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .findFirst()
                .orElse("Validation error");
        return build(HttpStatus.BAD_REQUEST, msg);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> unhandled(Exception ex) {
        log.error("Unhandled exception", ex);           // full stack trace in logs
        return build(HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal server error. Please try again later.");
    }

    /* ---------- helpers ---------- */

    private ResponseEntity<ErrorResponse> build(HttpStatus status, Exception ex) {
        return build(status, ex.getMessage());
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(new ErrorResponse(message));
    }
}
