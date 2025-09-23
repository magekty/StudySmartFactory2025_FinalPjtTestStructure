package com.factory_dynamics.erp.erp_server.bom.controller;

import com.factory_dynamics.erp.erp_server.bom.entity.BomHeader;
import com.factory_dynamics.erp.erp_server.bom.service.BomQueryService;
import com.factory_dynamics.erp.erp_server.bom.service.BomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/boms")
@Tag(name = "BOM", description = "BOM 관리 API")
public class BomController {

    private final BomService bomService;
    private final BomQueryService bomQueryService;

    public BomController(BomService bomService, BomQueryService bomQueryService) {
        this.bomService = bomService;
        this.bomQueryService = bomQueryService;
    }

    @Operation(summary = "BOM 헤더 생성")
    @PostMapping
    public ResponseEntity<BomHeader> createHeader(
            @RequestParam @NotBlank String productId,
            @RequestParam(defaultValue = "R1") String revision,
            @RequestParam(defaultValue = "true") boolean active,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate effectiveFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate effectiveTo,
            @RequestParam(required = false) String note
    ) {
        return ResponseEntity.ok(bomService.createHeader(productId, revision, active, effectiveFrom, effectiveTo, note, "system"));
    }

}