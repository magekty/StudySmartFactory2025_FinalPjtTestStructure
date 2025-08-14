// src/main/java/com/globalmed/mes/mes_api/workorder/WorkOrderQueryController.java
package com.globalmed.mes.mes_api.workorder;

import com.globalmed.mes.mes_api.common.PageResponse;
import com.globalmed.mes.mes_api.workorder.dto.WorkOrderListDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

import static com.globalmed.mes.mes_api.workorder.WorkOrderSpecs.*; // ← 정적 임포트 중요

@RestController
@RequestMapping("/work-orders")
@RequiredArgsConstructor
public class WorkOrderQueryController {

    private final WorkOrderRepo workOrderRepo;

    @GetMapping(params = {"page","size"})
    public ResponseEntity<PageResponse<WorkOrderListDto>> list(
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort,
            @RequestParam(required = false) String equipmentId,
            @RequestParam(required = false) String status, // "P"|"R"|"C"
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        String[] sp = sort.split(",");
        Sort s = (sp.length == 2 && "asc".equalsIgnoreCase(sp[1]))
                ? Sort.by(sp[0]).ascending()
                : Sort.by(sp[0]).descending();
        Pageable pageable = PageRequest.of(page, size, s);

        Specification<WorkOrderEntity> spec = Specification.allOf(
                equipmentIdEquals(equipmentId),
                statusEquals(status),
                startBetween(from, to)
        );

        Page<WorkOrderEntity> result = workOrderRepo.findAll(spec, pageable);

        Page<WorkOrderListDto> dtoPage = result.map(wo -> new WorkOrderListDto(
                wo.getWorkOrderId(),
                wo.getWorkOrderNumber(),
                wo.getItemId(),
                wo.getProcessId(),
                wo.getEquipmentId(),
                wo.getOrderQty(),
                wo.getProducedQty(),
                (wo.getStatusCode() != null ? wo.getStatusCode().getCode() : null)
        ));

        return ResponseEntity.ok(PageResponse.of(dtoPage, sort));
    }
}