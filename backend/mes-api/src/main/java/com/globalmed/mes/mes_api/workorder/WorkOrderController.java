// com.globalmed.mes.mes_api.workorder.WorkOrderController.java
package com.globalmed.mes.mes_api.workorder;


import com.globalmed.mes.mes_api.common.PageResponse;
import com.globalmed.mes.mes_api.workorder.dto.WorkOrderListDto;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/work-orders")
@RequiredArgsConstructor
public class WorkOrderController {
    private final WorkOrderService svc;
    private final WorkOrderRepo repo;

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
    @PutMapping("/{id}/status")
    public ResponseEntity<?> changeStatus(@PathVariable("id") String workOrderId,
                                          @RequestBody StatusChangeReq req) {
        var wo = svc.transition(workOrderId, req.toStatus());
        return ResponseEntity.ok(Map.of("workOrderId", wo.getWorkOrderId(),
                "status", wo.getStatusCode().getCode()));
    }

    public record StatusChangeReq(@NotBlank String toStatus) {}



}