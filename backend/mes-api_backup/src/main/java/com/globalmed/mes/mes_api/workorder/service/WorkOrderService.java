// src/main/java/com/globalmed/mes/mes_api/workorder/service/WorkOrderService.java
package com.globalmed.mes.mes_api.workorder.service;

import com.globalmed.mes.mes_api.code.CodeRepo;
import com.globalmed.mes.mes_api.log.ProdLogService;
import com.globalmed.mes.mes_api.workorder.domain.WorkOrderEntity;
import com.globalmed.mes.mes_api.workorder.repository.WorkOrderRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkOrderService {

    private static final String GROUP_WO_STATUS = "WO_STATUS";

    private final WorkOrderRepo woRepo;
    private final CodeRepo codeRepo;
    private final ProdLogService prodLogService;

    @Transactional
    public WorkOrderEntity create(String workOrderNumber, String itemId, String processId,
                                  String equipmentId, BigDecimal orderQty, String createdByOpt) {

        woRepo.findByWorkOrderNumber(workOrderNumber).ifPresent(x -> {
            throw new IllegalStateException("DUPLICATE_KEY");
        });

        // 상태 코드 존재 검증(코드 테이블 기준) - FK 저장 아님
        codeRepo.findByGroupCodeAndCodeAndUseYn(GROUP_WO_STATUS, "P", 'Y')
                .orElseThrow(() -> new IllegalStateException("WO_STATUS_P_NOT_FOUND"));

        var wo = new WorkOrderEntity();
        wo.setWorkOrderId(UUID.randomUUID().toString());
        wo.setWorkOrderNumber(workOrderNumber);
        wo.setItemId(itemId);
        wo.setProcessId(processId);
        wo.setEquipmentId(equipmentId);
        wo.setOrderQty(orderQty);
        wo.setProducedQty(BigDecimal.ZERO);
        // 문자열 상태코드로 저장(엔티티 내부 필드는 VARCHAR(1) 가정)
        wo.setStatusCode("P");

        if (createdByOpt != null && !createdByOpt.isBlank()) {
            wo.setCreatedBy(createdByOpt);
        }

        return woRepo.save(wo);
    }

    @Transactional
    public WorkOrderEntity transition(String workOrderId, String toStatus) {
        var wo = woRepo.findById(workOrderId)
                .orElseThrow(() -> new IllegalArgumentException("NOT_FOUND"));

        String cur = normalize(wo.getStatusCode());               // 현재 'P' | 'R' | 'C'
        String to  = normalize(toStatus);

        boolean allowed = ("P".equals(cur) && "R".equals(to))
                || ("R".equals(cur) && "C".equals(to));
        if (!allowed) throw new IllegalStateException("WO_STATUS_INVALID");

        // 코드 유효성 검증(코드 테이블) - 저장은 문자열만
        codeRepo.findByGroupCodeAndCodeAndUseYn(GROUP_WO_STATUS, to, 'Y')
                .orElseThrow(() -> new IllegalStateException("WO_STATUS_" + to + "_NOT_FOUND"));

        wo.setStatusCode(to);  // 문자열 코드로 세팅

        // 듀얼 라이트(상태 변경 로그)
        prodLogService.workOrderStatus(
                workOrderId,
                to,
                OffsetDateTime.now(ZoneOffset.UTC),
                "WO-STS:" + workOrderId + ":" + to
        );

        return wo; // @Transactional 컨텍스트에서 플러시
    }

    @Transactional
    public void changeStatus(String woId, String nextStatus, OffsetDateTime changedUtc) {
        String to = normalize(nextStatus);

        // 코드 유효성 검증(코드 테이블)
        codeRepo.findByGroupCodeAndCodeAndUseYn(GROUP_WO_STATUS, to, 'Y')
                .orElseThrow(() -> new IllegalStateException("WO_STATUS_" + to + "_NOT_FOUND"));

        prodLogService.workOrderStatus(
                woId,
                to,
                (changedUtc != null ? changedUtc.withOffsetSameInstant(ZoneOffset.UTC) : OffsetDateTime.now(ZoneOffset.UTC)),
                "WO-STS:" + woId + ":" + to
        );
    }

    private String normalize(String s) {
        return s == null ? "" : s.trim().toUpperCase();
    }
}