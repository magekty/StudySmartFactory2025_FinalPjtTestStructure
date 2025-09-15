package com.factory_dynamics.erp.erp_server.bom;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/boms/lines")
@Tag(name = "BOM-Line", description = "BOM 라인 CUD API")
public class BomLineCommandController {

    private final BomLineCommandService cmdSvc;

    public BomLineCommandController(BomLineCommandService cmdSvc) {
        this.cmdSvc = cmdSvc;
    }

    @Operation(summary = "BOM 라인 추가")
    @PostMapping
    public ResponseEntity<BomLineResponse> add(@Valid @RequestBody BomLineCreateRequest req) {
        return ResponseEntity.ok(cmdSvc.addLine(req, "system"));
    }

    @Operation(summary = "BOM 라인 삭제(논리)")
    @DeleteMapping("/{lineId}")
    public ResponseEntity<Void> delete(@PathVariable String lineId) {
        cmdSvc.removeLine(lineId, "system");
        return ResponseEntity.noContent().build();
    }
}