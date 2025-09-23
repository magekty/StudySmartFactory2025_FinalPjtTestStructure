// src/main/java/com/factory_dynamics/erp/erp_server/bom/BomLineCommandController.java
package com.factory_dynamics.erp.erp_server.bom.controller;

import com.factory_dynamics.erp.erp_server.bom.BomLineCreateRequest;
import com.factory_dynamics.erp.erp_server.bom.BomLineResponse;
import com.factory_dynamics.erp.erp_server.bom.service.BomLineCommandService;
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

    @Operation(summary = "BOM 라인 추가(중복 시 소프트삭제 레코드 리사이클)")
    @PostMapping
    public ResponseEntity<BomLineResponse> add(@Valid @RequestBody BomLineCreateRequest req,
                                               @RequestHeader(value = "X-Actor", required = false) String actor) {
        var who = actor == null ? "system" : actor;
        return ResponseEntity.ok(cmdSvc.addLineWithRecycle(req, who));
    }

    @Operation(summary = "BOM 라인 삭제(논리)")
    @DeleteMapping("/{lineId}")
    public ResponseEntity<Void> delete(@PathVariable String lineId,
                                       @RequestHeader(value = "X-Actor", required = false) String actor) {
        var who = actor == null ? "system" : actor;
        cmdSvc.removeLine(lineId, who);
        return ResponseEntity.noContent().build();
    }
}