// com.globalmed.mes.mes_api.workorder.WorkOrderController.java
package com.globalmed.mes.mes_api.workorder;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.globalmed.mes.mes_api.common.DateTimeMapper;
import com.globalmed.mes.mes_api.workorder.dto.WorkOrderDetailDto;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;

@RestController
@RequestMapping("/work-orders")
@RequiredArgsConstructor
public class WorkOrderController {
    private final WorkOrderService svc;
    private final WorkOrderRepo workOrderRepo;

    @PreAuthorize("hasAnyRole('ADMIN','OP')")
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
    @PreAuthorize("hasAnyRole('ADMIN','OP')")
    @PutMapping("/{id}/status")
    public ResponseEntity<?> changeStatus(@PathVariable("id") String workOrderId,
                                          @RequestBody StatusChangeReq req) {
        var wo = svc.transition(workOrderId, req.toStatus());
        return ResponseEntity.ok(Map.of("workOrderId", wo.getWorkOrderId(),
                "status", wo.getStatusCode().getCode()));
    }
    @GetMapping("/{id}")
    public ResponseEntity<WorkOrderDetailDto> get(@PathVariable String id) {
        var wo = workOrderRepo.findById(id).orElseThrow();

        // startTs는 네가 “이미 정상”이라 했으니 건드리지 않음(원하면 동일 패턴으로 toKst 적용 가능)
        OffsetDateTime startKst   = null; // 또는 DateTimeMapper.attachKst(wo.getStartTs());
        OffsetDateTime createdKst = DateTimeMapper.attachKst(wo.getCreatedAt());
        OffsetDateTime modifiedKst= DateTimeMapper.attachKst(wo.getModifiedAt());

        var dto = new WorkOrderDetailDto(
                wo.getWorkOrderId(),
                wo.getWorkOrderNumber(),
                wo.getItemId(),
                wo.getProcessId(),
                wo.getEquipmentId(),
                wo.getOrderQty(),
                wo.getProducedQty(),
                wo.getStatusCode() != null ? wo.getStatusCode().getCode() : null,
                startKst,
                createdKst,
                modifiedKst
        );
        return ResponseEntity.ok(dto);
    }

//    // '값이 이미 KST 시각으로 읽혀온' LocalDateTime에 +09:00 오프셋만 부착 (순간 변환 없음)
//    private static OffsetDateTime toKstAttach(LocalDateTime ldt) {
//        if (ldt == null) return null;
//        return OffsetDateTime.of(ldt, ZoneOffset.ofHours(9));
//    }



    public record StatusChangeReq(@NotBlank String toStatus) {}




}