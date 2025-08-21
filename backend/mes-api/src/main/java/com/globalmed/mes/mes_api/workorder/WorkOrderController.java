// com.globalmed.mes.mes_api.workorder.WorkOrderController.java
package com.globalmed.mes.mes_api.workorder;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.globalmed.mes.mes_api.workorder.dto.WorkOrderDetailDto;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Map;

@RestController
@RequestMapping("/work-orders")
@RequiredArgsConstructor
public class WorkOrderController {
    private final WorkOrderService svc;
    private final WorkOrderRepo repo;

    @PreAuthorize("@permChecker.has(authentication, '/work-orders','write') or hasAnyRole('ADMIN','OP')")
    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateReq req){
        var wo = svc.create(
                req.workOrderNumber(), req.itemId(), req.processId(),
                req.equipmentId(), req.orderQty(), req.createdBy() // null 가능
        );
        return ResponseEntity.status(201)
                .body(Map.of("workOrderId", wo.getWorkOrderId(), "status", wo.getStatusCode().getCode()));
    }

    public record CreateReq(
            @NotBlank String workOrderNumber,
            @NotBlank String itemId,
            @NotBlank String processId,
            @NotBlank String equipmentId,
            @NotNull @DecimalMin("0") BigDecimal orderQty,
            String createdBy // 선택, 없으면 @PrePersist가 채움
    ){}

    // 상태 전이: P->R, R->C
    @PreAuthorize("@permChecker.has(authentication, '/work-orders','write') or hasAnyRole('ADMIN','OP')")
    @PutMapping("/{id}/status")
    public ResponseEntity<?> changeStatus(@PathVariable("id") String workOrderId,
                                          @RequestBody StatusChangeReq req) {
        var wo = svc.transition(workOrderId, req.toStatus());
        return ResponseEntity.ok(Map.of("workOrderId", wo.getWorkOrderId(),
                "status", wo.getStatusCode().getCode()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkOrderDetailDto> get(@PathVariable String id){
        var wo = repo.findById(id).orElseThrow();

        // 엔티티 LocalDateTime 값을 “UTC 벽시계”로 간주 → 오프셋만 UTC로 부여
        // src/main/java/.../workorder/WorkOrderController.java (상세)
        var startTsUtc = com.globalmed.mes.mes_api.common.DateTimeMapper.attachKst(wo.getStartTs());
        var createdUtc  = com.globalmed.mes.mes_api.common.DateTimeMapper.attachKst(wo.getCreatedAt());
        var modifiedUtc = com.globalmed.mes.mes_api.common.DateTimeMapper.attachKst(wo.getModifiedAt());
        // DTO 생성 시 createdAt/modifiedAt에 위 값 전달

        var dto = new WorkOrderDetailDto(
                wo.getWorkOrderId(),
                wo.getWorkOrderNumber(),
                wo.getItemId(),
                wo.getProcessId(),
                wo.getEquipmentId(),
                wo.getOrderQty(),
                wo.getProducedQty(),
                wo.getStatusCode()!=null ? wo.getStatusCode().getCode() : null,
                startTsUtc,
                createdUtc,
                modifiedUtc
        );
        return ResponseEntity.ok(dto);
    }

    public record StatusChangeReq(@NotBlank String toStatus) {}




}