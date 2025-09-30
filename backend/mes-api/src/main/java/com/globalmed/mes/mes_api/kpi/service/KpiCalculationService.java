package com.globalmed.mes.mes_api.kpi.service;

import com.globalmed.mes.mes_api.code.Definition.service.DefinitionService;
import com.globalmed.mes.mes_api.kpi.dto.KpiDataParams;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class KpiCalculationService {

    private final DefinitionService definitionService;

    /**
     * Performance 기반 양품/불량/총 생산 수 집계
     */
    public Map<String, BigDecimal> calculateFromPerformance(BigDecimal goodQty, BigDecimal defectQty, BigDecimal runSeconds, BigDecimal plannedSeconds) {
        BigDecimal totalQty = goodQty.add(defectQty);
        BigDecimal yield = calculateYieldFromValues(goodQty, totalQty);
        BigDecimal defectRate = calculateDefectRateFromValues(defectQty, totalQty);
        BigDecimal oee = calculateOeeFromValues(goodQty, totalQty, runSeconds, plannedSeconds);
        BigDecimal productivity = calculateProductivity(totalQty, runSeconds);

        return Map.of(
                "yield", yield,
                "defectRate", defectRate,
                "oee", oee,
                "productivity", productivity
        );
    }

    /**
     * 생산성 계산
     */
    public BigDecimal calculateProductivity(BigDecimal producedQty, BigDecimal runSeconds) {
        //초 기준에서 시간 기준으로 전환
        BigDecimal runHours = runSeconds.divide(BigDecimal.valueOf(3600), 4, RoundingMode.HALF_UP);
        KpiDataParams params = new KpiDataParams()
                .setProducedQty(producedQty)
                .setRunTime(runHours);
        return definitionService.calculate("Productivity", params.toMap());
    }

    /**
     * OEE 계산
     * 임시 메서드: 직접 값으로 계산
     */
    public BigDecimal calculateOeeFromValues(BigDecimal goodQty, BigDecimal totalQty, BigDecimal runSeconds, BigDecimal plannedSeconds) {
        KpiDataParams params = new KpiDataParams()
                .setGoodQty(goodQty)
                .setTotalQty(totalQty)
                .setRunTime(runSeconds)
                .setPlannedTime(plannedSeconds);
        return definitionService.calculate("OEE", params.toMap());
    }

    /**
     * 수율(Yield) 계산
     */
    public BigDecimal calculateYieldFromValues(BigDecimal goodQty, BigDecimal totalQty) {
        KpiDataParams params = new KpiDataParams()
                .setGoodQty(goodQty)
                .setTotalQty(totalQty);
        return definitionService.calculate("Yield", params.toMap());
    }

    /**
     * 불량률(Defect Rate) 계산
     */
    public BigDecimal calculateDefectRateFromValues(BigDecimal defectQty, BigDecimal totalQty) {
        KpiDataParams params = new KpiDataParams()
                .setDefectQty(defectQty)
                .setTotalQty(totalQty);
        return definitionService.calculate("Defect Rate", params.toMap());
    }
}
