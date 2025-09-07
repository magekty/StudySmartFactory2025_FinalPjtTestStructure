// src/main/java/com/demo/erp/web/mock/MockExceptionAdvice.java
package com.demo.erp.web.mock;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.format.DateTimeParseException;
import java.util.NoSuchElementException;

@Slf4j
@Profile("dev")
@RestControllerAdvice
public class MockExceptionAdvice {

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<String> handleNotFound(NoSuchElementException e) {
        log.warn("[MOCK-ERP] 404 {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    @ExceptionHandler({IllegalArgumentException.class, DateTimeParseException.class})
    public ResponseEntity<String> handleBadRequest(RuntimeException e) {
        log.warn("[MOCK-ERP] 400 {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleOthers(Exception e) {
        log.error("[MOCK-ERP] 500", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("INTERNAL_ERROR");
    }
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<String> mismatch(MethodArgumentTypeMismatchException e) {
        return ResponseEntity.badRequest().body("BAD_REQUEST: " + e.getMessage());
    }
}