package com.factory_dynamics.erp.erp_server.mes_adapter.service;

// ProductionPlanService.java
import com.factory_dynamics.erp.erp_server.mes_adapter.api.MesApiClient;
import com.factory_dynamics.erp.erp_server.mes_adapter.dto.ProductionPlanDto;
import com.factory_dynamics.erp.erp_server.plan.ProductionPlan;
import com.factory_dynamics.erp.erp_server.plan.ProductionPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ToMesProductionPlanService {

    private final ProductionPlanRepository planRepository; // JPA Repository 가정
    private final MesApiClient mesApiClient;

    // ERP 상태 코드 상수 (실제 Enum 사용 권장)
    private static final String CONFIRMED = "CONFIRMED";
    private static final String PENDING = "PENDING";

    /**
     * CONFIRMED 상태의 생산 계획을 MES로 전송하고 PENDING으로 상태를 변경합니다.
     * @param planId 전송할 계획 ID
     * @param modifier 수정자 (로그인 사용자)
     * @throws MesApiClient.MesIntegrationException MES 연동 실패 시
     */
    @Transactional
    public void sendPlanToMes(String planId, String modifier) {
        // 1. 계획 조회 및 CONFIRMED 상태 확인
        ProductionPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new NoSuchElementException("계획을 찾을 수 없습니다: " + planId));

        if (!CONFIRMED.equals(plan.getStatus())) {
            throw new IllegalStateException("CONFIRMED 상태의 계획만 MES로 전송할 수 있습니다. 현재 상태: " + plan.getStatus());
        }

        // 2. MES 전송을 위한 DTO 생성 (Entity에서 DTO로 변환)
        ProductionPlanDto planDto = new ProductionPlanDto(
                plan.getId(),
                plan.getPlanCode(),
                plan.getProduct().getId(), // product_id가 item_id로 매핑된다고 가정
                plan.getQty(),
                plan.getStartDate(),
                plan.getEndDate(),
                modifier
        );

        // 3. MES API 호출 (비동기 처리: 블로킹 필요 시 .block() 사용)
        try {
            // WebClient의 Mono를 블로킹하여 동기적으로 처리 (트랜잭션 유지를 위해)
            mesApiClient.sendProductionPlan(planDto).block();

            // 4. MES 전송 성공 시, ERP 상태를 PENDING으로 업데이트
            plan.setStatus(PENDING);
            plan.setModifiedBy(modifier);
            // planRepository.save(plan); // save 호출은 @Transactional에 의해 자동으로 처리될 수 있음

        } catch (MesApiClient.MesIntegrationException e) {
            // MES 연동 실패 시, 트랜잭션을 롤백하고 예외를 던져 상태 변경을 막음
            throw e;
        }
    }
}