// src/main/java/com/globalmed/mes/mes_api/recon/ReconciliationController.java
package com.globalmed.mes.mes_api.recon;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/internal/recon")
@RequiredArgsConstructor
public class ReconciliationController {

    private final DailyReconciliationService svc;

    @PostMapping("/run")
    public ResponseEntity<?> runOn(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateKst) {
        Map<String, Map<String, String>> res = svc.runForKst(dateKst);
        return ResponseEntity.ok(res);
    }
}