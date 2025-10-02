package com.factory_dynamics.erp.erp_server.plan.controller;

import com.factory_dynamics.erp.erp_server.mes_adapter.service.ToMesProductionPlanService; // 🚨 추가
import com.factory_dynamics.erp.erp_server.plan.ProductionPlanService;
import com.factory_dynamics.erp.erp_server.plan.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor; // Lombok 사용 권장
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/plans")
@Tag(name = "Plan", description = "생산 계획 API")
@RequiredArgsConstructor // final 필드에 대한 생성자 자동 생성
public class ProductionPlanController {

    private final ProductionPlanService svc;
    private final ToMesProductionPlanService toMesService; // 🚨 MES 전송 서비스 추가

    // 기존 생성자는 Lombok의 @RequiredArgsConstructor로 대체 가능
    // public ProductionPlanController(ProductionPlanService svc) {
    //     this.svc = svc;
    // }

    @Operation(summary = "목록 조회")
    @GetMapping
    public ResponseEntity<Page<ProductionPlanSummaryDto>> list(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "ALL") String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort
    ) {
        var sortObj = Sort.by(sort.split(",")[1].equalsIgnoreCase("desc") ? Sort.Order.desc(sort.split(",")[0]) : Sort.Order.asc(sort.split(",")[0]));
        return ResponseEntity.ok(svc.list(q, status, from, to, page, size, sortObj));
    }

    @Operation(summary = "상세 조회")
    @GetMapping("/{planId}")
    public ResponseEntity<ProductionPlanDetailDto> get(@PathVariable String planId) {
        return ResponseEntity.ok(svc.get(planId));
    }

    @Operation(summary = "생성")
    @PostMapping
    public ResponseEntity<ProductionPlanDetailDto> create(@RequestBody CreateProductionPlanRequest req) {
        return ResponseEntity.ok(svc.create(req, "system"));
    }

    @Operation(summary = "수정")
    @PutMapping("/{planId}")
    public ResponseEntity<ProductionPlanDetailDto> update(@PathVariable String planId, @RequestBody UpdateProductionPlanRequest req) {
        return ResponseEntity.ok(svc.update(planId, req, "system"));
    }

    @Operation(summary = "상태 전이")
    @PatchMapping("/{planId}/status")
    public ResponseEntity<ProductionPlanDetailDto> changeStatus(@PathVariable String planId, @RequestBody ChangeStatusRequest req) {
        return ResponseEntity.ok(svc.changeStatus(planId, req));
    }

    @Operation(summary = "삭제(논리)")
    @DeleteMapping("/{planId}")
    public ResponseEntity<Void> delete(@PathVariable String planId) {
        svc.softDelete(planId, "system");
        return ResponseEntity.noContent().build();
    }

    // ----------------------------------------------------------------------
    // 🚨 MES 연동 기능 추가: CONFIRMED 계획을 MES로 전송
    // ----------------------------------------------------------------------
    /**
     * 프론트엔드 호출: POST /api/plans/{planId}/send-to-mes
     */
    @Operation(summary = "MES 전송", description = "확정된 계획을 MES로 전송하고, 상태를 PENDING으로 변경")
    @PostMapping("/{planId}/send-to-mes")
    public ResponseEntity<ProductionPlanDetailDto> sendPlanToMes(@PathVariable String planId,
                                                                 @RequestBody MesTransferRequest req) {

        // 1. MES 전송 및 ERP 상태 업데이트 (PENDING)
        // Service는 Plan Entity를 업데이트하고, MES API를 호출합니다.
        // Service에서 업데이트된 Plan DTO를 반환하도록 가정합니다.
        ProductionPlanDetailDto updatedDto = toMesService.sendPlanToMes(planId, req.modifier());

        // 2. 업데이트된 Plan 상세 정보를 프론트엔드에 반환
        return ResponseEntity.ok(updatedDto);
    }

    /**
     * C# 프론트엔드의 SendToMesRequest에 매핑될 요청 DTO (Inner Class)
     */
    public record MesTransferRequest(String modifier) {}
}