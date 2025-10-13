package com.factory_dynamics.erp.erp_server.mes_adapter.controller;

import com.factory_dynamics.erp.erp_server.mes_adapter.dto.WorkOrderStatusFeedbackDto;
import com.factory_dynamics.erp.erp_server.mes_adapter.service.MesPlanFeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mes/feedback")
@RequiredArgsConstructor
public class MesPlanFeedbackController {

    private final MesPlanFeedbackService feedbackService;

    /**
     * MES로부터 Work Order 상태 변경 피드백을 수신하여 ERP ProductionPlan 상태를 업데이트합니다.
     */
    @PostMapping("/status") // 🚨 MES ErpApiClient가 호출하는 경로
    public ResponseEntity<String> updatePlanStatus(@RequestBody WorkOrderStatusFeedbackDto feedbackDto) {

        feedbackService.updatePlanStatus(feedbackDto);

        // MES에 성공적으로 처리되었음을 알림
        return ResponseEntity.ok("ERP Plan status updated successfully.");
    }
}