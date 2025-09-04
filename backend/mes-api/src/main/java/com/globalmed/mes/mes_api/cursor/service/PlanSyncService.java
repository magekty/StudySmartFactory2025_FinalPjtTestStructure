// src/main/java/com/globalmed/mes/mes_api/cursor/service/PlanSyncService.java
package com.globalmed.mes.mes_api.cursor.service;

import com.globalmed.mes.mes_api.cursor.domain.SyncCursorEntity;
import com.globalmed.mes.mes_api.cursor.repository.SyncCursorRepository;
import com.globalmed.mes.mes_api.integration.erp.ErpIncrementalClient;
import com.globalmed.mes.mes_api.integration.erp.dto.PlanLineDto;
import com.globalmed.mes.mes_api.plan.domain.ProductionPlanEntity;
import com.globalmed.mes.mes_api.plan.domain.ProductionPlanLineEntity;
import com.globalmed.mes.mes_api.plan.repository.ProductionPlanLineRepository;
import com.globalmed.mes.mes_api.plan.repository.ProductionPlanRepository;
import com.globalmed.mes.mes_api.sync.SyncAuditLogger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlanSyncService {

    private static final String CURSOR_KEY = "erp_plans";
    private final ErpIncrementalClient client;
    private final ProductionPlanLineRepository lineRepo;
    private final ProductionPlanRepository planRepo;
    private final SyncCursorRepository cursorRepo;
    private final SyncAuditLogger audit;

    @Transactional
    public void sync() {
        OffsetDateTime repoCursor = cursorRepo.findById(CURSOR_KEY)
                .map(SyncCursorEntity::getLastSyncedAt)                 // LocalDateTime(UTC 저장)
                .map(dt -> dt.atOffset(ZoneOffset.UTC))                 // → OffsetDateTime(UTC)
                .orElse(Instant.EPOCH.atOffset(ZoneOffset.UTC));

        OffsetDateTime nowUtc = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime since = repoCursor.isAfter(nowUtc) ? nowUtc.minusSeconds(1) : repoCursor.minusSeconds(1);

        int page = 0, size = 100;
        OffsetDateTime processedMaxTs = since;
        int fetchedTotal = 0, upserts = 0, deletes = 0;

        long auditId = audit.start("PLANS", since.toInstant(), nowUtc.toInstant());

        try {
            while (true) {
                List<PlanLineDto> list = client.plans(since, page, size);
                int got = (list == null ? 0 : list.size());
                fetchedTotal += got;
                log.info("[PLAN_SYNC] page={} got={}", page, got);
                if (got == 0) break;

                for (PlanLineDto p : list) {
                    ensureHeader(p);
                    upsertLine(p);
                    if (Boolean.TRUE.equals(p.isDeleted())) deletes++; else upserts++;
                    if (p.updatedAt() != null && p.updatedAt().isAfter(processedMaxTs)) {
                        processedMaxTs = p.updatedAt();
                    }
                }
                if (got < size) break;
                page++;
            }

            if (processedMaxTs.isAfter(repoCursor)) {
                OffsetDateTime saveTs = processedMaxTs.isAfter(nowUtc) ? nowUtc : processedMaxTs;
                SyncCursorEntity c = cursorRepo.findById(CURSOR_KEY)
                        .orElseGet(() -> SyncCursorEntity.initAtEpoch(CURSOR_KEY));
                c.setLastSyncedAt(saveTs.withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime());
                cursorRepo.save(c);
                log.info("[PLAN_SYNC] done fetched={} upserts={} deletes={} newCursor={}",
                        fetchedTotal, upserts, deletes, saveTs);
            } else {
                log.info("[PLAN_SYNC] done fetched={} upserts={} deletes={} cursorUnchanged={}",
                        fetchedTotal, upserts, deletes, repoCursor);
            }

            audit.success(auditId, fetchedTotal, upserts, deletes, "OK");
        } catch (Exception e) {
            audit.fail(auditId, e.toString());
            throw e;
        }
    }

    private void ensureHeader(PlanLineDto p) {
        if (planRepo.existsById(p.planId())) return;
        java.sql.Date day = java.sql.Date.valueOf(
                p.dueDateUtc().withOffsetSameInstant(ZoneOffset.UTC).toLocalDate()
        );
        planRepo.save(ProductionPlanEntity.seed(p.planId(), p.itemId(), day));
    }

    private void upsertLine(PlanLineDto p) {
        ProductionPlanLineEntity line = lineRepo.findByPlanIdAndPlanLineNo(p.planId(), p.planLineNo())
                .orElseGet(ProductionPlanLineEntity::new);

        if (line.getPlanLineId() == null) {
            line.setPlanId(p.planId());
            line.setPlanLineNo(p.planLineNo());
            line.setCreatedBy("sync");
            line.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        }

        line.setItemId(p.itemId());
        line.setQty(BigDecimal.valueOf(p.qty() == null ? 0d : p.qty()));
        line.setUnit((p.unit() == null || p.unit().isBlank()) ? "EA" : p.unit());
        line.setDueDate(p.dueDateUtc());
        line.setPriority(p.priority());
        if (p.isDeleted()) { line.setIsDeleted(true); line.setDeletedAt(OffsetDateTime.now(ZoneOffset.UTC)); }
        else { line.setIsDeleted(false); line.setDeletedAt(null); }
        line.setModifiedBy("sync");
        line.setModifiedAt(OffsetDateTime.now(ZoneOffset.UTC));

        lineRepo.save(line);
    }
}