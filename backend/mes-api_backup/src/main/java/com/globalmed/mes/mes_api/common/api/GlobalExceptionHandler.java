package com.globalmed.mes.mes_api.common.api;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpStatusCodeException;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// common/api/GlobalExceptionHandler.java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req){
        Map<String,Object> details = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(e -> details.put(e.getField(), e.getDefaultMessage()));
        return ResponseEntity.badRequest().body(error("VALIDATION_ERROR","필드 검증 실패",details, req));
    }
    @ExceptionHandler(HttpStatusCodeException.class)
    public ResponseEntity<ErrorResponse> handleHttpStatus(HttpStatusCodeException ex, HttpServletRequest req) {
        return ResponseEntity.status(ex.getStatusCode())
                .body(error("ERROR", ex.getMessage(), null, req));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleEtc(Exception ex, HttpServletRequest req) {
        return ResponseEntity.status(500)
                .body(error("SERVER_ERROR", ex.getMessage(), null, req));
    }

    private ErrorResponse error(String code,String msg,Map<String,Object> details,HttpServletRequest req){
        return ErrorResponse.builder()
                .code(code).message(msg).details(details)
                .traceId(UUID.randomUUID().toString().substring(0,12))
                .timestamp(OffsetDateTime.now(ZoneOffset.UTC).toString())
                .path(req.getRequestURI()).method(req.getMethod()).build();
    }
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> badReq(IllegalArgumentException e, HttpServletRequest req){
        return ResponseEntity.badRequest().body(Map.of(
                "code", e.getMessage().equals("NOT_FOUND") ? "NOT_FOUND" : "VALIDATION_ERROR",
                "message", e.getMessage(),
                "path", req.getRequestURI(), "method", req.getMethod()
        ));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<?> illegalState(IllegalStateException e, HttpServletRequest req){
        int status = "DUPLICATE_KEY".equals(e.getMessage()) ? 409 : 400;
        return ResponseEntity.status(status).body(Map.of(
                "code", e.getMessage(),
                "message", e.getMessage(),
                "path", req.getRequestURI(),
                "method", req.getMethod()
        ));
    }
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<?> accessDenied(HttpServletRequest req) {
        return ResponseEntity.status(403).body(Map.of(
                "code","FORBIDDEN",
                "message","Access Denied",
                "path", req.getRequestURI(),
                "method", req.getMethod()
        ));
    }
}