// src/main/java/com/globalmed/mes/mes_api/internal/outbox/OutboxController.java
package com.globalmed.mes.mes_api.internal.outbox;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/internal/outbox")
@RequiredArgsConstructor
public class OutboxController {

    private final OutboxQueryService svc;

    public record SummaryView(List<OutboxQueryService.Summary> byStatus) {}
    public record ListView(List<OutboxQueryService.Row> rows) {}

    @GetMapping("/summary")
    public ResponseEntity<SummaryView> summary() {
        return ResponseEntity.ok(new SummaryView(svc.summary()));
    }

    @GetMapping
    public ResponseEntity<ListView> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) Integer sinceMinutes,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false, name = "searchWo") String searchWo
    ) {
        return ResponseEntity.ok(new ListView(svc.list(status, eventType, sinceMinutes, limit, searchWo)));
    }
}