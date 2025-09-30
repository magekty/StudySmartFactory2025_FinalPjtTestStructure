package com.globalmed.mes.mes_api.kpi.service;

import com.globalmed.mes.mes_api.code.CodeRepo;
import com.globalmed.mes.mes_api.kpi.domain.KpiDataEntity;
import com.globalmed.mes.mes_api.kpi.repository.KpiDataRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class KpiDataService {

    private final KpiDataRepo kpiDataRepo;
    private final CodeRepo codeRepo;
    private final KpiCalculationService kpiCalculationService;

    private static final String KPI_DATA_TYPE_GROUP = "KPI_DATA_TYPE";
    private static final String KPI_CALC_STATUS_GROUP = "KPI_CALC_STATUS";

    // 공통 코드 ID 검색 메서드
    public Long getCodeId(String groupCode, String code) {
        return codeRepo.findByGroupCodeAndCode(groupCode, code)
                .orElseThrow(() -> new IllegalArgumentException("Invalid code: " + groupCode + " - " + code))
                .getCodeId();
    }

    public Long getDailyBatchAggregationTypeId() {
        return getCodeId(KPI_DATA_TYPE_GROUP, "DAILY_BATCH");
    }

    public Long getRealtimeAggregationTypeId() {
        return getCodeId(KPI_DATA_TYPE_GROUP, "REALTIME");
    }

    public Long getSuccessCalcStatusCodeId() {
        return getCodeId(KPI_CALC_STATUS_GROUP, "SUCCESS");
    }

    /**
     * KPI 값을 계산하고 KpiDataEntity에 업데이트 후 저장하는 공통 로직
     */
    @Transactional
    public void calculateAndSave(KpiDataEntity kpiData, BigDecimal totalGoodQty, BigDecimal totalDefectQty, BigDecimal runSeconds, BigDecimal plannedSeconds) {
        // KpiCalculationService를 사용하여 KPI 값 계산
        Map<String, BigDecimal> kpiValues = kpiCalculationService.calculateFromPerformance(
                totalGoodQty, totalDefectQty, runSeconds, plannedSeconds
        );

        // 엔티티에 계산된 값 설정
        kpiData.setActualYield(kpiValues.get("yield"));
        kpiData.setActualDefectRate(kpiValues.get("defectRate"));
        kpiData.setActualOee(kpiValues.get("oee"));
        kpiData.setActualProductivity(kpiValues.get("productivity"));

        // 공통 속성 설정
        kpiData.setCalcStatusCodeId(getSuccessCalcStatusCodeId());
        kpiData.setCalcAt(LocalDateTime.now());
        kpiData.setCreatedBy("system");

        // 데이터베이스에 저장
        kpiDataRepo.save(kpiData);
    }
}
