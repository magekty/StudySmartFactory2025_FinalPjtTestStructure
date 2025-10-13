package com.factory_dynamics.erp.erp_server.mes_adapter.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * MES로부터 Work Order 상태 변경 시 피드백 받는 DTO
 */
public record WorkOrderStatusFeedbackDto(
        String planId,              // ERP의 ProductionPlan ID
        String workOrderId,
        String workOrderNumber,
        String newPlanStatus,       // MES가 판단한 ERP의 최종 Plan 상태 (예: RUNNING, FINISHED)
        String mesWoStatusCode,     // MES Work Order의 현재 상태 코드
        BigDecimal totalProducedQty, // Work Order의 누적 생산 수량
        OffsetDateTime updateTime
) {}