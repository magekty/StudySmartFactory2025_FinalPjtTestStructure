package com.globalmed.mes.mes_api.kpi.service;

import com.globalmed.mes.mes_api.kpi.domain.KpiDataEntity;
import com.globalmed.mes.mes_api.kpi.downtime.service.PlannedDowntimeService;
import com.globalmed.mes.mes_api.kpi.downtime.service.UnplannedDowntimeService;
import com.globalmed.mes.mes_api.kpi.repository.KpiDataRepo;
import com.globalmed.mes.mes_api.performance.domain.ProductionPerformanceEntity;
import com.globalmed.mes.mes_api.performance.repository.PerformanceRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
@Service
@RequiredArgsConstructor
public class RealTimeKpiService {

    private final KpiDataService kpiDataService;
    private final KpiDataRepo kpiDataRepo;
    private final PerformanceRepo performanceRepo;
    private final PlannedDowntimeService plannedDowntimeService;
    private final UnplannedDowntimeService unplannedDowntimeService;

    @Transactional
    public void saveKpiFromPerformance(ProductionPerformanceEntity newPerformance) {
        // 1. 주어진 작업 순서에 대한 모든 성과 기록을 가져옵니다.
        List<ProductionPerformanceEntity> allPerformancesForWorkOrder = performanceRepo.findPerformancesByWorkOrder(
                newPerformance.getWorkOrder().getWorkOrderId(),
                newPerformance.getEquipment().getEquipmentId(),
                newPerformance.getProcess().getProcessId(),
                newPerformance.getItem().getItemId()
        );

        if (allPerformancesForWorkOrder.isEmpty()) {
            return;
        }

        // 2. 모든 데이터 포인트를 축적합니다.
        BigDecimal totalGoodQty = BigDecimal.ZERO;
        BigDecimal totalDefectQty = BigDecimal.ZERO;
        LocalDateTime firstStartTime = null;
        LocalDateTime lastEndTime = null;

        for (ProductionPerformanceEntity p : allPerformancesForWorkOrder) {
            totalGoodQty = totalGoodQty.add(p.getProducedQty().subtract(p.getDefectQty()));
            totalDefectQty = totalDefectQty.add(p.getDefectQty());
            if (firstStartTime == null || p.getStartTime().isBefore(firstStartTime)) {
                firstStartTime = p.getStartTime();
            }
            if (lastEndTime == null || p.getEndTime().isAfter(lastEndTime)) {
                lastEndTime = p.getEndTime();
            }
        }

        // 3. 누적된 시간을 기준으로 계획된/계획되지 않은 다운타임을 계산합니다.
        long totalPeriodSeconds = Duration.between(firstStartTime, lastEndTime).toSeconds();
        long plannedDowntimeSeconds = plannedDowntimeService.calculatePlannedDowntimeSeconds(newPerformance.getEquipment().getEquipmentId(),
                firstStartTime,
                lastEndTime);

        long unplannedDowntimeSeconds = unplannedDowntimeService.calculateUnplannedDowntimeSeconds(newPerformance.getEquipment().getEquipmentId(),
                firstStartTime.atOffset(ZoneOffset.UTC),
                lastEndTime.atOffset(ZoneOffset.UTC));

        BigDecimal plannedSeconds = BigDecimal.valueOf(totalPeriodSeconds - plannedDowntimeSeconds );
        BigDecimal runSeconds = plannedSeconds.subtract(BigDecimal.valueOf(unplannedDowntimeSeconds));
        // 4. 기존 KPI 기록을 찾거나 새로 만듭니다.
        Optional<KpiDataEntity> existingKpi =  kpiDataRepo.findRealtimeKpiByWorkOrderId(
                newPerformance.getWorkOrder().getWorkOrderId(),
                newPerformance.getEquipment().getEquipmentId(),
                newPerformance.getProcess().getProcessId(),
                newPerformance.getItem().getItemId(),
                kpiDataService.getRealtimeAggregationTypeId()
        );

        KpiDataEntity kpi = existingKpi.orElseGet(KpiDataEntity::new);
        kpi.setKpiDate(newPerformance.getStartTime().toLocalDate());
        kpi.setEquipmentId(newPerformance.getEquipment().getEquipmentId());
        kpi.setProcessId(newPerformance.getProcess().getProcessId());
        kpi.setItemId(newPerformance.getItem().getItemId());
        kpi.setWorkOrderId(newPerformance.getWorkOrder().getWorkOrderId());
        kpi.setAggregationTypeId(kpiDataService.getRealtimeAggregationTypeId());
        kpi.setBatchGroupKey(null);
        kpi.setStartTime(firstStartTime);
        kpi.setEndTime(lastEndTime);

        // 5. 공통 서비스 호출하여 계산 및 저장
        kpiDataService.calculateAndSave(kpi, totalGoodQty, totalDefectQty, runSeconds, plannedSeconds);
    }
}
