// src/main/java/com/demo/erp/web/ErpController.java
package com.demo.erp.web;
import com.demo.erp.web.dto.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/erp") // ← 클래스 레벨 베이스 경로 고정
public class ErpController {

    private final IdempotencyService idem;
    public ErpController(IdempotencyService idem) { this.idem = idem; }

    private ResponseEntity<?> replayOrNull(HttpServletRequest r) {
        return idem.replayIfPresent(r.getMethod(), r.getRequestURI(), r.getHeader("X-Idempotency-Key"));
    }
    private void store(HttpServletRequest r, ResponseEntity<?> res) {
        idem.store(r.getMethod(), r.getRequestURI(), r.getHeader("X-Idempotency-Key"), res);
    }

    @PostMapping("/work-orders")
    public ResponseEntity<?> createWo(@RequestBody @Valid WorkOrderCreateDto dto, HttpServletRequest r) {
        var replay = replayOrNull(r); if (replay != null) return replay;
        if (dto.startTs()!=null && dto.startTs().getOffset().getTotalSeconds()!=0)
            throw new IllegalArgumentException("VALIDATION_ERROR");
        var res = ResponseEntity.status(201).body(Map.of("workOrderId", dto.workOrderId()));
        store(r, res); return res;
    }

    @PutMapping("/work-orders/{workOrderId}/status")
    public ResponseEntity<?> status(@PathVariable String workOrderId, @RequestBody @Valid WorkOrderStatusDto dto, HttpServletRequest r) {
        var replay = replayOrNull(r); if (replay != null) return replay;
        if (dto.changedAt().getOffset().getTotalSeconds()!=0)
            throw new IllegalArgumentException("VALIDATION_ERROR");
        var res = ResponseEntity.ok(Map.of("workOrderId", workOrderId, "status", dto.status()));
        store(r, res); return res;
    }

    @PostMapping("/performances")
    public ResponseEntity<?> perf(@RequestBody @Valid PerformanceCreateDto dto, HttpServletRequest r) {
        var replay = replayOrNull(r); if (replay != null) return replay;
        if (dto.endTime().isBefore(dto.startTime())) throw new IllegalArgumentException("TIME_ORDER_INVALID");
        if (dto.startTime().getOffset().getTotalSeconds()!=0 || dto.endTime().getOffset().getTotalSeconds()!=0)
            throw new IllegalArgumentException("VALIDATION_ERROR");
        var res = ResponseEntity.status(201).body(Map.of("performanceId", UUID.randomUUID().toString().replace("-","")));
        store(r, res); return res;
    }

    @PostMapping("/consumptions/backflush")
    public ResponseEntity<?> backflush(@RequestBody @Valid BackflushDto dto, HttpServletRequest r) {
        var replay = replayOrNull(r); if (replay != null) return replay;
        if (dto.lines()==null || dto.lines().isEmpty())
            throw new IllegalArgumentException("VALIDATION_ERROR");
        var res = ResponseEntity.status(202).body(Map.of("workOrderId", dto.workOrderId(), "shadow", "handled-by-MES"));
        store(r, res); return res;
    }
}