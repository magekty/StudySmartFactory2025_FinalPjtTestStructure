// src/main/java/com/demo/erp/web/RestExceptionHandler.java
package com.demo.erp.web;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import java.util.Map;

@RestControllerAdvice
public class RestExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> bad(IllegalArgumentException e, HttpServletRequest r) {
        String code = e.getMessage() == null || e.getMessage().isBlank() ? "VALIDATION_ERROR" : e.getMessage();
        return ResponseEntity.badRequest().body(Map.of(
                "code", code, "message", code, "path", r.getRequestURI(), "method", r.getMethod(), "timestamp", OffsetDateTime.now().toString()
        ));
    }
}