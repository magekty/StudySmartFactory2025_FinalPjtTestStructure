package com.globalmed.mes.mes_api.kpi;


import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.*;

public interface KpiTargetRepo extends JpaRepository<KpiTargetEntity, Long> {
    // 설비 기준 첫 타깃(여러 공정/품목이 있으면 임의 1건 선택)
    Optional<KpiTargetEntity> findFirstByKpiDateAndEquipmentId(LocalDate kpiDate, String equipmentId);
}