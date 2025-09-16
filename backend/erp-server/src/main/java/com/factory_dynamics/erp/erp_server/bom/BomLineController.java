// 8) Java BE - Controller: 라인 조회는 서비스 경유
package com.factory_dynamics.erp.erp_server.bom;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/boms")
public class BomLineController {

    private final BomQueryService queryService;

    public BomLineController(BomQueryService queryService) {
        this.queryService = queryService;
    }

    @GetMapping("/{bomId}/lines")
    public ResponseEntity<List<BomLineResponse>> lines(@PathVariable String bomId) {
        return ResponseEntity.ok(queryService.getLines(bomId));
    }
}