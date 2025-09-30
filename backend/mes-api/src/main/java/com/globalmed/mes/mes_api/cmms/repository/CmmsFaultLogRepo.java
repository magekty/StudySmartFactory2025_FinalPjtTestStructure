package com.globalmed.mes.mes_api.cmms.repository;

import com.globalmed.mes.mes_api.cmms.domain.CmmsFaultLog;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;

public interface CmmsFaultLogRepo extends JpaRepository<CmmsFaultLog, Long> {
    @Query("""
            select f from CmmsFaultLog f
            where f.deleted=false
              and (:equipmentId is null or f.equipmentId = :equipmentId)
              and (:from is null or f.occurredAt >= :from)
              and (:to is null or f.occurredAt < :to)
            """)
    Page<CmmsFaultLog> search(@Param("equipmentId") String equipmentId,
                              @Param("from") OffsetDateTime from,
                              @Param("to") OffsetDateTime to,
                              Pageable pageable);
    // DowntimeService에 필요한 메서드 추가
    long countByEquipmentIdAndOccurredAtBetween(String equipmentId, OffsetDateTime from, OffsetDateTime to);

}
