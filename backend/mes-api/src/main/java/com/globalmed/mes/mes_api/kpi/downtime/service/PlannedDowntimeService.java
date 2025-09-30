package com.globalmed.mes.mes_api.kpi.downtime.service;

import com.globalmed.mes.mes_api.code.CodeEntity;
import com.globalmed.mes.mes_api.code.CodeRepo;
import com.globalmed.mes.mes_api.kpi.downtime.domain.PlannedDowntimeEntity;
import com.globalmed.mes.mes_api.kpi.downtime.dto.DowntimeIntervalDto;
import com.globalmed.mes.mes_api.kpi.downtime.dto.PlannedDowntimeDto;
import com.globalmed.mes.mes_api.kpi.downtime.repository.PlannedDowntimeRepo;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 계획된 다운타임(Planned Downtime) 관련 계산을 전담하는 서비스입니다.
 * PM, 교대조 휴식 시간 등 모든 계획된 다운타임 데이터를 처리합니다.
 */
@Service
@RequiredArgsConstructor
public class PlannedDowntimeService {

    private final PlannedDowntimeRepo plannedDowntimeRepo;
    private final CodeRepo codeRepo;

    /**
     * 새로운 계획된 다운타임 기록을 추가합니다.
     */
    @Transactional
    public PlannedDowntimeEntity addPlannedDowntime(PlannedDowntimeDto dto) {
        // 코드 값으로 CodeEntity 조회
        CodeEntity downtimeType = codeRepo.findByGroupCodeAndCodeAndUseYn("PLANNED_DOWNTIME_TYPE", dto.getDowntimeTypeCode(), 'Y')
                .orElseThrow(() -> new EntityNotFoundException("Downtime type code not found: " + dto.getDowntimeTypeCode()));

        OffsetDateTime startTz = dto.getStartTime()
                .atZone(ZoneId.systemDefault()) // 서버 시간대 기준
                .toOffsetDateTime()             // OffsetDateTime으로 변환
                .withOffsetSameInstant(ZoneOffset.UTC); // UTC 기준
        OffsetDateTime endTz = dto.getEndTime()
                .atZone(ZoneId.systemDefault())
                .toOffsetDateTime()
                .withOffsetSameInstant(ZoneOffset.UTC);


        if (endTz.isBefore(startTz)) throw new IllegalArgumentException("TIME_ORDER_INVALID");

        PlannedDowntimeEntity plannedDowntimeEntity = new PlannedDowntimeEntity();
        plannedDowntimeEntity.setEquipmentId(dto.getEquipmentId());
        plannedDowntimeEntity.setStartTime(startTz);
        plannedDowntimeEntity.setEndTime(endTz);
        plannedDowntimeEntity.setDowntimeTypeCodeId(downtimeType.getCodeId());
        plannedDowntimeEntity.setDescription(dto.getDescription());

        // 다운타임 시간 계산 (초 단위)
        Duration duration = Duration.between(startTz, endTz);
        plannedDowntimeEntity.setDurationSeconds(duration.getSeconds());

        return plannedDowntimeRepo.save(plannedDowntimeEntity);
    }

    /**
     * 주어진 장비와 기간에 대한 총 계획된 다운타임 시간을 초 단위로 계산
     */
    @Transactional
    public long calculatePlannedDowntimeSeconds(String equipmentId, LocalDateTime start, LocalDateTime end) {
        OffsetDateTime startTz = start
                .atZone(ZoneId.systemDefault()) // 서버 시간대 기준
                .toOffsetDateTime()             // OffsetDateTime으로 변환
                .withOffsetSameInstant(ZoneOffset.UTC); // UTC 기준
        OffsetDateTime endTz = end
                .atZone(ZoneId.systemDefault())
                .toOffsetDateTime()
                .withOffsetSameInstant(ZoneOffset.UTC);

        // 주어진 기간 내의 모든 계획된 다운타임 기록을 조회
        List<PlannedDowntimeEntity> downtimes = plannedDowntimeRepo.findByEquipmentIdAndOverlappingDateRange(
                equipmentId, startTz, endTz
        );
        long totalSeconds = 0L;
        for (PlannedDowntimeEntity downtime : downtimes) {
            OffsetDateTime intervalStart = downtime.getStartTime();
            OffsetDateTime intervalEnd = downtime.getEndTime();

            // 계산 기간과 다운타임 기간이 겹치는 부분만 계산
            OffsetDateTime effectiveStart = intervalStart.isAfter(startTz) ? intervalStart : startTz;
            OffsetDateTime effectiveEnd = intervalEnd.isBefore(endTz) ? intervalEnd : endTz;

            if (effectiveStart.isBefore(effectiveEnd) || effectiveStart.isEqual(effectiveEnd)) {
                Duration duration = Duration.between(effectiveStart, effectiveEnd);
                totalSeconds += duration.toSeconds();
            }
        }
        return totalSeconds;
    }

    /**
     * 주어진 기간과 겹치는 모든 계획된 다운타임 기록의 시간 간격을 조회
     * 이 메서드는 UnplannedDowntimeService에서 사용하기 위해 DTO 리스트를 반환
     */
    @Transactional
    public List<DowntimeIntervalDto> getPlannedDowntimeIntervals(String equipmentId, LocalDateTime from, LocalDateTime to) {
        OffsetDateTime fromTz = from
                .atZone(ZoneId.systemDefault()) // 서버 시간대 기준
                .toOffsetDateTime()             // OffsetDateTime으로 변환
                .withOffsetSameInstant(ZoneOffset.UTC); // UTC 기준
        OffsetDateTime toTz = to
                .atZone(ZoneId.systemDefault())
                .toOffsetDateTime()
                .withOffsetSameInstant(ZoneOffset.UTC);
        List<PlannedDowntimeEntity> downtimes = plannedDowntimeRepo.findByEquipmentIdAndOverlappingDateRange(
                equipmentId, fromTz, toTz
        );
        return downtimes.stream()
                .map(d -> new DowntimeIntervalDto(d.getStartTime().atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime(), d.getEndTime().atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime()))
                .collect(Collectors.toList());
    }
}
