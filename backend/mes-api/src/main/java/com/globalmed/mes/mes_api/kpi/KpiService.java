package com.globalmed.mes.mes_api.kpi;

import com.globalmed.mes.mes_api.performance.PerformanceRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;

@Service
@RequiredArgsConstructor
public class KpiService {

    private final KpiTargetRepo targetRepo;
    private final PerformanceRepo perfRepo;

    public Res getActuals(LocalDate kpiDate, String equipmentId) {
        // 집계 구간(UTC) [start, nextDay)
        LocalDateTime from = kpiDate.atStartOfDay();
        LocalDateTime to   = kpiDate.plusDays(1).atStartOfDay();

        var agg = perfRepo.aggregateForDay(equipmentId, from, to);
        BigDecimal produced = (agg != null && agg.getProduced() != null) ? agg.getProduced() : BigDecimal.ZERO;
        BigDecimal good     = (agg != null && agg.getGood()     != null) ? agg.getGood()     : BigDecimal.ZERO;

        BigDecimal actualOutput = produced.setScale(4, RoundingMode.HALF_UP);
        BigDecimal actualYield  = produced.compareTo(BigDecimal.ZERO) == 0
                ? null
                : good.multiply(BigDecimal.valueOf(100))
                .divide(produced, 2, RoundingMode.HALF_UP); // % 소수2

        var targetOpt = targetRepo.findFirstByKpiDateAndEquipmentId(kpiDate, equipmentId);
        BigDecimal tOee  = targetOpt.map(KpiTargetEntity::getTargetOee).orElse(null);
        BigDecimal tProd = targetOpt.map(KpiTargetEntity::getTargetProductivity).orElse(null);
        BigDecimal tYield= targetOpt.map(KpiTargetEntity::getTargetYield).orElse(null);

        return new Res(kpiDate.toString(), equipmentId, tOee, tProd, tYield, actualOutput, actualYield);
    }

    public record Res(
            String kpiDate, String equipmentId,
            BigDecimal targetOee, BigDecimal targetProductivity, BigDecimal targetYield,
            BigDecimal actualOutput, BigDecimal actualYield
    ){}
}