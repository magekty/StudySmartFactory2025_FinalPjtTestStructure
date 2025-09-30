package com.globalmed.mes.mes_api.cmms.repository;

import com.globalmed.mes.mes_api.cmms.domain.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface CmmsWorkOrderRepo extends JpaRepository<CmmsWorkOrder, Long> {
    Optional<CmmsWorkOrder> findByRequestId(String requestId);
    Optional<CmmsWorkOrder> findByIdAndDeletedFalse(Long id);

    @Query("""
        select w from CmmsWorkOrder w
        where w.deleted=false
          and (:statusCodeId is null or w.statusCodeId = :statusCodeId)
          and (:equipmentId is null or w.equipmentId = :equipmentId)        
    """)
    Page<CmmsWorkOrder> search(@Param("statusCodeId") Long statusCodeId,
                               @Param("equipmentId") String equipmentId,
                               Pageable pageable);

    // DowntimeService에 필요한 메서드 추가
    List<CmmsWorkOrder> findByEquipmentIdAndStatusCodeIdAndFinishedAtBetween(String equipmentId, Long statusCodeId, OffsetDateTime from, OffsetDateTime to);

}
