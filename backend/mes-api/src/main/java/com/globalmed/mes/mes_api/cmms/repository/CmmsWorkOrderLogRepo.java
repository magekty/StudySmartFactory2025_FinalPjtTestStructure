package com.globalmed.mes.mes_api.cmms.repository;

import com.globalmed.mes.mes_api.cmms.domain.CmmsWorkOrderLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CmmsWorkOrderLogRepo extends JpaRepository<CmmsWorkOrderLog, Long> {
    List<CmmsWorkOrderLog> findByCmmsWoIdOrderByChangedAtAsc(Long cmmsWoId);
}
