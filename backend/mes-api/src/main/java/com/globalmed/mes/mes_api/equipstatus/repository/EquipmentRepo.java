package com.globalmed.mes.mes_api.equipstatus.repository;

import com.globalmed.mes.mes_api.equipstatus.domain.EquipmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EquipmentRepo extends JpaRepository<EquipmentEntity, String> {
    @Query("SELECT e FROM EquipmentEntity e JOIN FETCH e.workcenter w WHERE e.equipmentId = :equipmentId AND e.isDeleted = false")
    EquipmentEntity findDetail(@Param("equipmentId") String equipmentId);
    @Query("""
    SELECT e
    FROM EquipmentEntity e
    JOIN FETCH e.workcenter w
    WHERE e.process.processId = :processId
      AND e.isDeleted = false
    """)
    List<EquipmentEntity> findEquipByPro(@Param("processId") String processId);

    @Query("SELECT e.equipmentId FROM EquipmentEntity e WHERE e.isDeleted = false")
    List<String> findAllEquipmentIds();
}
