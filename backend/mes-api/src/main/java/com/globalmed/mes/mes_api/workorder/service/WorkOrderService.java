// com.globalmed.mes.mes_api.workorder.service.WorkOrderService.java
package com.globalmed.mes.mes_api.workorder.service;


import com.globalmed.mes.mes_api.code.CodeRepo;
import com.globalmed.mes.mes_api.process.repository.ProcessRepo;
import com.globalmed.mes.mes_api.item.ItemRepo;
import com.globalmed.mes.mes_api.employee.cert.service.ProcessCertCheckService;
import com.globalmed.mes.mes_api.equipstatus.repository.EquipmentRepo;
import com.globalmed.mes.mes_api.production.service.ProductionLogService;
import com.globalmed.mes.mes_api.workorder.domain.WorkOrderEntity;
import com.globalmed.mes.mes_api.workorder.dto.WorkOrderDetailDto;
import com.globalmed.mes.mes_api.workorder.dto.WorkOrderListDto;
import com.globalmed.mes.mes_api.workorder.repository.WorkOrderRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkOrderService {
    private final WorkOrderRepo woRepo;
    private final CodeRepo codeRepo;
    private final ItemRepo itemRepo;
    private final ProcessRepo processRepo;
    private final EquipmentRepo equipmentRepo;
    private final ProductionLogService productionLogService;
    private final ProcessCertCheckService processCertCheckService;
    @Transactional
    public WorkOrderEntity create(String workOrderNumber, String itemId, String processId,
                                  String equipmentId, BigDecimal orderQty, String createdByOpt) {

        woRepo.findByWorkOrderNumber(workOrderNumber).ifPresent(x -> {
            throw new IllegalStateException("DUPLICATE_KEY");
        });

        // 1. 각 ID로 관련 엔티티 객체 조회
        var item = itemRepo.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("ITEM_NOT_FOUND"));
        var process = processRepo.findById(processId)
                .orElseThrow(() -> new IllegalArgumentException("PROCESS_NOT_FOUND"));
        var equipment = equipmentRepo.findById(equipmentId)
                .orElseThrow(() -> new IllegalArgumentException("EQUIPMENT_NOT_FOUND"));

        // 상태코드 P, use_yn='Y'
        var status = codeRepo.findByGroupCodeAndCodeAndUseYn("WO_STATUS", "P", 'Y')
                .orElseThrow(() -> new IllegalStateException("WO_STATUS_P_NOT_FOUND"));

        var wo = new WorkOrderEntity();
        wo.setWorkOrderId(UUID.randomUUID().toString());
        wo.setWorkOrderNumber(workOrderNumber);
        wo.setItemId(item);
        wo.setProcessId(process);
        wo.setEquipmentId(equipment);
        wo.setOrderQty(orderQty);
        wo.setProducedQty(BigDecimal.ZERO);
        wo.setStatusCode(status);        // ← status_code_id 매핑 완료
        if (createdByOpt != null && !createdByOpt.isBlank()) {
            wo.setCreatedBy(createdByOpt); // 값 있으면 사용, 없으면 @PrePersist에서 자동 세팅
        }

        return woRepo.save(wo);
    }

    @Transactional
    public WorkOrderEntity transition(String workOrderId, String toStatus, OffsetDateTime now) {
        var wo = woRepo.findById(workOrderId)
                .orElseThrow(() -> new IllegalArgumentException("NOT_FOUND"));

        var cur = wo.getStatusCode().getCode();         // 현재 P/R/C
        var to  = toStatus != null ? toStatus.trim() : "";

        // 허용 전이만 통과
        boolean allowed = (cur.equals("P") && to.equals("R"))
                || (cur.equals("R") && to.equals("C"));
        if (!allowed) {
            throw new IllegalStateException("WO_STATUS_INVALID");
        }
        if(now == null) now = OffsetDateTime.now();
//        P -> R 전이 공정 자격 체크 (개발용으로 임시 비활성화)
        if(cur.equals("P")&&to.equals("R")){
            // TODO: 실제 운영 환경에서는 아래 주석을 해제하고 위의 주석을 제거하세요
            // processCertCheckService.check(wo.getEquipmentId().getEquipmentId(),wo.getProcessId().getProcessId(), now);
        }

        // 상태 코드(P/R/C) 조회(use_yn='Y'), group_code는 네 DB 기준으로(소문자/대문자)
        var next = codeRepo.findByGroupCodeAndCodeAndUseYn("wo_status", to, 'Y')
                .orElseThrow(() -> new IllegalStateException("WO_STATUS_"+to+"_NOT_FOUND"));

        wo.setStatusCode(next);           // status_code_id 매핑

        // ✅ 상태 전이에 따른 로그 기록
        if (cur.equals("P") && to.equals("R")) {
            // Released → START: startTs 설정
            wo.setStartTs(now.toLocalDateTime());
            // START 로그 기록
            productionLogService.logStart(
                    wo.getWorkOrderId(),
                    wo.getEquipmentId().getEquipmentId(),
                    wo.getProcessId().getProcessId()
            );
        } else if (cur.equals("R") && to.equals("C")) {
            // Completed → END: endTs 설정
            wo.setEndTs(now.toLocalDateTime());
            // END 로그 기록
            productionLogService.logEnd(
                    wo.getWorkOrderId(),
                    wo.getEquipmentId().getEquipmentId(),
                    wo.getProcessId().getProcessId()
            );
        }
        return wo;
    }
    // @Transactional로 플러시
    @Transactional
    public WorkOrderDetailDto findById(String workOrderId) {
        var wo = woRepo.findByIdWithDetails(workOrderId)
                .orElseThrow(() -> new IllegalArgumentException("NOT_FOUND"));
        return WorkOrderDetailDto.fromEntity(wo);
    }

    @Transactional
    public List<WorkOrderListDto> findAllWithDetails(Specification<WorkOrderEntity> spec) {
        List<WorkOrderEntity> filtered = woRepo.findAll(spec);

        List<WorkOrderEntity> workOrders = woRepo.findAllwithDetails();

        // 엔티티 리스트를 DTO 리스트로 변환
        return workOrders.stream()
                .map(WorkOrderListDto::fromEntity)
                .collect(Collectors.toList());
    }
}