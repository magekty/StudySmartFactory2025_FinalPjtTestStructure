// src/main/java/com/globalmed/mes/mes_api/bom/repository/BomLineRepository.java
package com.globalmed.mes.mes_api.bom.repository;

import com.globalmed.mes.mes_api.bom.domain.BomLineEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BomLineRepository extends JpaRepository<BomLineEntity, Long> {
    Optional<BomLineEntity> findByHeader_BomIdAndLineNo(String bomId, Integer lineNo);
}