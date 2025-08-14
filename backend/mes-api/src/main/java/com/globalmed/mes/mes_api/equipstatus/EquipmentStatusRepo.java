package com.globalmed.mes.mes_api.equipstatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface EquipmentStatusRepo extends JpaRepository<EquipmentStatusLogEntity, Long> {
    Page<EquipmentStatusLogEntity> findByEquipmentIdOrderByStartTimeDesc(String equipmentId, Pageable pageable);
    Page<EquipmentStatusLogEntity> findByEquipmentIdAndStartTimeBetweenOrderByStartTimeDesc(
            String equipmentId, LocalDateTime from, LocalDateTime to, Pageable pageable);
}