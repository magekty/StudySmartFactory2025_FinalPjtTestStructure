package com.globalmed.mes.mes_api.performance;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/performances")
@RequiredArgsConstructor
public class PerformanceController {

    private final PerformanceService svc;

    @PreAuthorize("@permChecker.has(authentication, '/performances','write') or hasAnyRole('ADMIN','OP')")
    @PostMapping
    public ResponseEntity<?> create(@RequestBody PerformanceService.Req req) {
        var res = svc.create(req);
        return ResponseEntity.status(201).body(Map.of(
                "performanceId", res.performanceId(),
                "goodQty", res.goodQty()
        ));
    }
}