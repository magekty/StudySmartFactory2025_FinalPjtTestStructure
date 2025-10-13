package com.factory_dynamics.erp.erp_server.mes_adapter.service;

import com.factory_dynamics.erp.erp_server.mes_adapter.dto.WorkOrderStatusFeedbackDto;
import com.factory_dynamics.erp.erp_server.plan.ProductionPlan;
import com.factory_dynamics.erp.erp_server.plan.ProductionPlanRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class MesPlanFeedbackService {

    private final ProductionPlanRepository planRepository;

    /**
     * MES 피드백을 기반으로 ProductionPlan의 상태를 업데이트합니다.
     * @param dto MES로부터 수신된 상태 피드백 DTO
     */
    @Transactional
    public void updatePlanStatus(WorkOrderStatusFeedbackDto dto) {

        // 1. Plan 조회 (planId는 ERP와 MES가 공유하는 UUID)
        ProductionPlan plan = planRepository.findById(dto.planId())
                .orElseThrow(() -> new NoSuchElementException("ERP에서 Plan을 찾을 수 없습니다: " + dto.planId()));

        // 2. 상태 변경 처리
        // MES가 보낸 'newPlanStatus'를 ERP의 상태로 사용합니다.
        String newStatus = dto.newPlanStatus();

        // 🚨 상태 전이 검증 로직 추가 (선택적)
        // 예: FINISHED 상태인데 다시 RUNNING으로 바뀌는 것을 방지
        if (isStatusTransitionValid(plan.getStatus(), newStatus)) {

            // 3. Plan 엔티티 업데이트
            plan.setStatus(newStatus);

            // TODO: 실제 생산 수량(producedQty) 등 다른 필드도 함께 업데이트 가능

            // 4. 저장 (낙관적 락 (@Version)이 여기서 동작)
            planRepository.save(plan);
        } else {
            // 경고 로그 또는 예외 처리
            System.err.println("경고: 유효하지 않은 상태 전이 요청: " + plan.getStatus() + " -> " + newStatus);
            // 요청을 무시하거나 롤백할 수 있습니다.
        }
    }

    /**
     * 간단한 상태 전이 유효성 검사 (예시)
     */
    private boolean isStatusTransitionValid(String currentStatus, String newStatus) {
        // FINISHED 상태는 더 이상 변경할 수 없음
        if ("FINISHED".equals(currentStatus)) {
            return false;
        }
        // PENDING 상태에서는 RUNNING 또는 CANCELLED로만 갈 수 있음 (비즈니스 규칙에 따라 정의)
        // ...
        return true;
    }
}