package com.globalmed.mes.mes_api.kpi.downtime.service;

import com.globalmed.mes.mes_api.kpi.downtime.dto.PlannedDowntimeDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class PMPlanDowntimeService {

    private final PlannedDowntimeService plannedDowntimeService;
    private static final String PM_DOWNTIME_CODE = "PM";

    @Transactional
    public void createPlannedDowntimeFromPmPlan(String equipmentId, OffsetDateTime startAt, int estimatedTakeTime, String description) {
        // 종료 시간 계산
        OffsetDateTime endAt = startAt.plusMinutes(estimatedTakeTime);

        // 기존 PlannedDowntimeService가 요구하는 DTO로 변환
        PlannedDowntimeDto plannedDowntimeDto = new PlannedDowntimeDto(
                equipmentId,
                startAt.toLocalDateTime(),
                endAt.toLocalDateTime(),
                PM_DOWNTIME_CODE,
                description
        );

        // 기존 서비스의 addPlannedDowntime 메서드 호출
        plannedDowntimeService.addPlannedDowntime(plannedDowntimeDto);
    }
}
