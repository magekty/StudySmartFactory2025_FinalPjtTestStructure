package com.globalmed.mes.mes_api.kpi.downtime.service;

import com.globalmed.mes.mes_api.kpi.downtime.dto.DowntimeIntervalDto;
import com.globalmed.mes.mes_api.production.domain.ProductionLogEntity;
import com.globalmed.mes.mes_api.production.repository.ProductionLogRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

/**
 * 계획되지 않은 다운타임(Unplanned Downtime) 관련 계산을 전담하는 서비스입니다.
 */
@Service
@RequiredArgsConstructor
public class UnplannedDowntimeService {

    private final ProductionLogRepo productionLogRepo;
    private final PlannedDowntimeService plannedDowntimeService;

    /**
     * ProductionLog 기록을 기반으로 계획되지 않은 비가동 시간(분)을 계산합니다.
     * 계획된 다운타임 시간을 제외하고 계산합니다.
     */
    @Transactional
    public long calculateUnplannedDowntimeSeconds(String equipmentId, OffsetDateTime start, OffsetDateTime end) {
        // 1. 주어진 기간 동안 해당 장비의 'DOWNTIME_END' 로그를 조회합니다.
        List<ProductionLogEntity> downtimeEndLogs = productionLogRepo.findByEquipmentIdAndEventType_CodeAndEventTimestampBetween(
                equipmentId, "DOWNTIME_END", start.toLocalDateTime(), end.toLocalDateTime());

        // 2. 해당 기간의 모든 계획된 다운타임 간격을 조회합니다.
        List<DowntimeIntervalDto> plannedDowntimeIntervals = plannedDowntimeService.getPlannedDowntimeIntervals(
                equipmentId, start.toLocalDateTime(), end.toLocalDateTime()
        );

        Long totalUnplannedDowntimeSeconds = 0L;

        for (ProductionLogEntity plog : downtimeEndLogs) {
            BigDecimal downtimeValue = plog.getEventValue();
            OffsetDateTime downtimeStart = plog.getEventTimestamp().atOffset(ZoneOffset.UTC).minusSeconds(downtimeValue.longValue());
            OffsetDateTime downtimeEnd = plog.getEventTimestamp().atOffset(ZoneOffset.UTC);

            // 3. 현재 비계획 다운타임이 계획된 다운타임과 겹치는지 확인하고, 겹치는 시간은 제외합니다.
            long overlapSeconds = 0L;
            for (DowntimeIntervalDto plannedInterval : plannedDowntimeIntervals) {
                // 겹치는 시간 계산
                OffsetDateTime plannedSt = plannedInterval.start().atOffset(ZoneOffset.UTC);
                OffsetDateTime plannedEnd = plannedInterval.end().atOffset(ZoneOffset.UTC);
                OffsetDateTime overlapStart = downtimeStart.isAfter(plannedSt) ? downtimeStart : plannedSt;
                OffsetDateTime overlapEnd = downtimeEnd.isBefore(plannedEnd) ? downtimeEnd : plannedEnd;

                if (overlapStart.isBefore(overlapEnd)) {
                    overlapSeconds += Duration.between(overlapStart, overlapEnd).getSeconds();
                }
            }

            // 겹치는 시간을 뺀 순수 비계획 다운타임만 합산
            long effectiveDowntimeSeconds = downtimeValue.longValue() - overlapSeconds;
            if (effectiveDowntimeSeconds > 0) {
                totalUnplannedDowntimeSeconds += effectiveDowntimeSeconds;
            }
        }
        return totalUnplannedDowntimeSeconds;
    }
}
