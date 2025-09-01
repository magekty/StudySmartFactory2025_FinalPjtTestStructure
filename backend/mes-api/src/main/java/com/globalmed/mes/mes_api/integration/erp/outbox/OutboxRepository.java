package com.globalmed.mes.mes_api.integration.erp.outbox;

import org.springframework.data.jpa.repository.*;
import java.time.OffsetDateTime;
import java.util.List;

public interface OutboxRepository extends JpaRepository<OutboxEntity, Long> {
    @Query("select o from OutboxEntity o where o.status in :statuses and (o.nextRetryAt is null or o.nextRetryAt <= :now) order by o.createdAt asc")
    List<OutboxEntity> pickBatch(List<OutboxStatus> statuses, OffsetDateTime now, org.springframework.data.domain.Pageable pageable);
}