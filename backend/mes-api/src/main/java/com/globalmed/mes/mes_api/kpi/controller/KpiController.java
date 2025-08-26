package com.globalmed.mes.mes_api.kpi.controller;


import com.globalmed.mes.mes_api.kpi.service.KpiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/kpi")
@RequiredArgsConstructor
public class KpiController {

    private final KpiService kpiService;

    // GET /kpi/actuals?kpiDate=2025-08-10&equipmentId=E-0001
    @GetMapping("/actuals")
    public ResponseEntity<?> actuals(@RequestParam String kpiDate,
                                     @RequestParam String equipmentId) {
        var date = LocalDate.parse(kpiDate); // ISO yyyy-MM-dd
        var res  = kpiService.getActuals(date, equipmentId);
        return ResponseEntity.ok(Map.of(
                "kpiDate", res.kpiDate(),
                "equipmentId", res.equipmentId(),
                "targetOee", res.targetOee(),
                "targetProductivity", res.targetProductivity(),
                "targetYield", res.targetYield(),
                "actualOutput", res.actualOutput(),
                "actualYield", res.actualYield()
        ));
    }
}