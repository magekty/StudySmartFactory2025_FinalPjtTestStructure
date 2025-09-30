// com.globalmed.mes.mes_api.workorder.controller.WorkOrderController.java
package com.globalmed.mes.mes_api.workorder.controller;


import com.globalmed.mes.mes_api.common.PageResponse;
import com.globalmed.mes.mes_api.workorder.domain.WorkOrderEntity;
import com.globalmed.mes.mes_api.workorder.repository.WorkOrderRepo;
import com.globalmed.mes.mes_api.workorder.service.WorkOrderService;
import com.globalmed.mes.mes_api.workorder.dto.WorkOrderDetailDto;
import com.globalmed.mes.mes_api.workorder.dto.WorkOrderListDto;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Map;

import static com.globalmed.mes.mes_api.workorder.specs.WorkOrderSpecs.*;

@RestController
@RequestMapping("/work-orders")
@RequiredArgsConstructor
public class WorkOrderController {
    private final WorkOrderService workOrderService;
    private final WorkOrderRepo workOrderRepo;

    @PreAuthorize("@permChecker.has(authentication, '/work-orders','write') or hasAnyRole('ADMIN','OP')")
    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateReq req){
        var wo = workOrderService.create(
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
                                          @RequestBody StatusChangeReq req,
                                          @RequestParam(name = "now", required = false) OffsetDateTime now) {
//        전이가드 테스트를 위해 offsetDateTime 추가
//        offsetDateTime이 null일시 현재 시간으로 계산
        var wo = workOrderService.transition(workOrderId, req.toStatus(), now);
        return ResponseEntity.ok(Map.of("workOrderId", wo.getWorkOrderId(),
                "status", wo.getStatusCode().getCode()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkOrderDetailDto> get(@PathVariable String id){

        var dto = workOrderService.findById(id);
        return ResponseEntity.ok(dto);
    }

    @GetMapping(params = {"page","size"})
    public ResponseEntity<PageResponse<WorkOrderListDto>> list(
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort,
            @RequestParam(required = false) String workOrderNumber,
            @RequestParam(required = false) String itemName,
            @RequestParam(required = false) String processName,
            @RequestParam(required = false) String equipmentName,
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
                workOrderNumberContains(workOrderNumber),
                itemNameContains(itemName),
                processNameContains(processName),
                equipmentNameContains(equipmentName),
                statusEquals(status),
                startBetween(from, to)
        );

        Page<WorkOrderEntity> result = workOrderRepo.findAll(spec, pageable);

        Page<WorkOrderListDto> dtoPage = result.map(WorkOrderListDto::fromEntity);

        return ResponseEntity.ok(PageResponse.of(dtoPage, sort));
    }

    public record StatusChangeReq(@NotBlank String toStatus) {}

}