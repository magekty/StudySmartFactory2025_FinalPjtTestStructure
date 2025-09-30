package com.globalmed.mes.mes_api.employee.cert.repository;

import com.globalmed.mes.mes_api.employee.cert.domain.EquipmentCertEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EquipmentCertRepo extends JpaRepository<EquipmentCertEntity, Long> {
    @Query("SELECT ec FROM EquipmentCertEntity ec WHERE ec.equipment.equipmentId = :equipmentId AND ec.deleted = false")
    List<EquipmentCertEntity> findEquiprequier(@Param("equipmentId") String equipmentId);



    boolean existsByEquipment_EquipmentId(String equipmentId);
}
