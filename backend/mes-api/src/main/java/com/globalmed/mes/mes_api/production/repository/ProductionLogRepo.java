package com.globalmed.mes.mes_api.production.repository;

import com.globalmed.mes.mes_api.production.domain.ProductionLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ProductionLogRepo extends JpaRepository<ProductionLogEntity, Long> {
    Optional<ProductionLogEntity> findTopByEquipmentIdAndEventType_CodeOrderByLogIdDesc(
            String equipmentId, String eventCode);
    List<ProductionLogEntity> findByEquipmentIdAndEventType_CodeAndEventTimestampBetween(
            String equipmentId, String eventCode, LocalDateTime start, LocalDateTime end);

}
