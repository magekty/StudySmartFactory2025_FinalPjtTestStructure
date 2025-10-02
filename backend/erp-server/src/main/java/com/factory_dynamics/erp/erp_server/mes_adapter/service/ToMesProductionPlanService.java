package com.factory_dynamics.erp.erp_server.mes_adapter.service;

import com.factory_dynamics.erp.erp_server.mes_adapter.api.MesApiClient;
import com.factory_dynamics.erp.erp_server.mes_adapter.api.MesApiClient.MesIntegrationException; // 예외 클래스 임포트
import com.factory_dynamics.erp.erp_server.mes_adapter.dto.ProductionPlanDto;
import com.factory_dynamics.erp.erp_server.plan.ProductionPlan;
import com.factory_dynamics.erp.erp_server.plan.ProductionPlanRepository;
import com.factory_dynamics.erp.erp_server.plan.dto.ProductionPlanDetailDto; // 🚨 반환 타입
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ToMesProductionPlanService {

    private final ProductionPlanRepository planRepository;
    private final MesApiClient mesApiClient;

    private static final String CONFIRMED = "CONFIRMED";
    private static final String PENDING = "PENDING";

    /**
     * CONFIRMED 상태의 생산 계획을 MES로 전송하고 PENDING으로 상태를 변경합니다.
     * @return 업데이트된 Plan 상세 DTO를 반환하여 Controller와 C# 프론트엔드의 요구사항을 충족합니다.
     */
    @Transactional
    public ProductionPlanDetailDto sendPlanToMes(String planId, String modifier) { // 🚨 반환 타입 수정
        // 1. 계획 조회 및 CONFIRMED 상태 확인
        ProductionPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new NoSuchElementException("계획을 찾을 수 없습니다: " + planId));

        if (!CONFIRMED.equals(plan.getStatus())) {
            throw new IllegalStateException("CONFIRMED 상태의 계획만 MES로 전송할 수 있습니다. 현재 상태: " + plan.getStatus());
        }

        // 2. MES 전송을 위한 DTO 생성
        ProductionPlanDto planDto = new ProductionPlanDto(
                plan.getId(),
                plan.getPlanCode(),
                plan.getProduct().getId(),
                plan.getQty(),
                plan.getStartDate(),
                plan.getEndDate(),
                modifier
        );

        // 3. MES API 호출
        try {
            mesApiClient.sendProductionPlan(planDto).block();

            // 4. MES 전송 성공 시, ERP 상태를 PENDING으로 업데이트
            plan.setStatus(PENDING);
            plan.setModifiedBy(modifier);
            // @Transactional에 의해 DB에 자동 반영됩니다.

        } catch (MesIntegrationException e) {
            // MES 연동 실패 시, 트랜잭션 롤백 및 예외 던지기
            throw e;
        }

        // 5. 업데이트된 엔티티를 DTO로 변환하여 반환
        return plan.toDetailDto(); // ProductionPlan에 toDetailDto()가 있다고 가정
    }
}