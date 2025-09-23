// src/main/java/com/factory_dynamics/erp/erp_server/bom/BomLineController.java
// 조회는 그대로(참고: fetch join 사용)
package com.factory_dynamics.erp.erp_server.bom.controller;

import com.factory_dynamics.erp.erp_server.bom.repository.BomLineRepository;
import com.factory_dynamics.erp.erp_server.bom.BomLineResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/boms")
public class BomLineController {

    private final BomLineRepository lineRepo;

    public BomLineController(BomLineRepository lineRepo) {
        this.lineRepo = lineRepo;
    }

    @GetMapping("/{bomId}/lines")
    public ResponseEntity<List<BomLineResponse>> lines(@PathVariable String bomId) {
        var lines = lineRepo.findLinesWithComponent(bomId).stream()
                .map(BomLineResponse::from).toList();
        return ResponseEntity.ok(lines);
    }
}