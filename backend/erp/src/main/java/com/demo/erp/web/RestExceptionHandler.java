// src/main/java/com/demo/erp/web/RestExceptionHandler.java
package com.demo.erp.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;

@RestControllerAdvice
public class RestExceptionHandler {

    private Map<String, Object> body(String code, HttpServletRequest r) {
        return Map.of(
                "code", code,
                "message", code,
                "path", r.getRequestURI(),
                "method", r.getMethod(),
                "timestamp", OffsetDateTime.now(ZoneOffset.UTC).toString()
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> badIllegal(IllegalArgumentException e, HttpServletRequest r) {
        String code = (e.getMessage()==null || e.getMessage().isBlank()) ? "VALIDATION_ERROR" : e.getMessage();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body(code, r));
    }

    @ExceptionHandler({ MethodArgumentNotValidException.class, ConstraintViolationException.class })
    public ResponseEntity<?> badValidation(Exception e, HttpServletRequest r) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body("VALIDATION_ERROR", r));
    }

    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<?> badReadable(Exception e, HttpServletRequest r) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body("VALIDATION_ERROR", r));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> internal(Exception e, HttpServletRequest r) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body("INTERNAL_ERROR", r));
    }
}