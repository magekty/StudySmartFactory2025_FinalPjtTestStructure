// src/main/java/com/globalmed/mes/mes_api/bom/repository/BomHeaderRepository.java
package com.globalmed.mes.mes_api.bom.repository;

import com.globalmed.mes.mes_api.bom.domain.BomHeaderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BomHeaderRepository extends JpaRepository<BomHeaderEntity, String> {
    Optional<BomHeaderEntity> findByItemIdAndRevisionAndAltCode(String itemId, String revision, String altCode);
}