// com.globalmed.mes.mes_api.workorder.WorkOrderService.java
package com.globalmed.mes.mes_api.workorder;


import com.globalmed.mes.mes_api.code.CodeRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkOrderService {
    private final WorkOrderRepo woRepo;
    private final CodeRepo codeRepo;

    @Transactional
    public WorkOrderEntity create(String workOrderNumber, String itemId, String processId,
                                  String equipmentId, BigDecimal orderQty, String createdByOpt) {

        woRepo.findByWorkOrderNumber(workOrderNumber).ifPresent(x -> {
            throw new IllegalStateException("DUPLICATE_KEY");
        });

        // 상태코드 P, use_yn='Y'
        var status = codeRepo.findByGroupCodeAndCodeAndUseYn("WO_STATUS", "P", 'Y')
                .orElseThrow(() -> new IllegalStateException("WO_STATUS_P_NOT_FOUND"));

        var wo = new WorkOrderEntity();
        wo.setWorkOrderId(UUID.randomUUID().toString());
        wo.setWorkOrderNumber(workOrderNumber);
        wo.setItemId(itemId);
        wo.setProcessId(processId);
        wo.setEquipmentId(equipmentId);
        wo.setOrderQty(orderQty);
        wo.setProducedQty(BigDecimal.ZERO);
        wo.setStatusCode(status);        // ← status_code_id 매핑 완료
        if (createdByOpt != null && !createdByOpt.isBlank()) {
            wo.setCreatedBy(createdByOpt); // 값 있으면 사용, 없으면 @PrePersist에서 자동 세팅
        }

        return woRepo.save(wo);
    }

    @Transactional
    public WorkOrderEntity transition(String workOrderId, String toStatus) {
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

        // 상태 코드(P/R/C) 조회(use_yn='Y'), group_code는 네 DB 기준으로(소문자/대문자)
        var next = codeRepo.findByGroupCodeAndCodeAndUseYn("wo_status", to, 'Y')
                .orElseThrow(() -> new IllegalStateException("WO_STATUS_"+to+"_NOT_FOUND"));

        wo.setStatusCode(next);           // status_code_id 매핑
        return wo;                        // @Transactional로 플러시
    }

}