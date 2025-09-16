// 10) Java BE - 트리 컨트롤러 (서비스 경유)
package com.factory_dynamics.erp.erp_server.bom;

import com.factory_dynamics.erp.erp_server.bom.dto.BomTreeNodeResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/boms")
public class BomTreeController {

    private final BomTreeQueryService svc;

    public BomTreeController(BomTreeQueryService svc) {
        this.svc = svc;
    }

    @GetMapping("/{bomId}/tree")
    public ResponseEntity<List<BomTreeNodeResponse>> getTree(@PathVariable String bomId) {
        return ResponseEntity.ok(svc.getTree(bomId));
    }
}