// src/main/java/com/globalmed/mes/mes_api/cursor/service/PlanSyncService.java
package com.globalmed.mes.mes_api.cursor.service;

import com.globalmed.mes.mes_api.cursor.repository.SyncCursorRepository;
import com.globalmed.mes.mes_api.cursor.domain.SyncCursorEntity;
import com.globalmed.mes.mes_api.integration.erp.ErpIncrementalClient;
import com.globalmed.mes.mes_api.integration.erp.dto.PlanLineDto;
import com.globalmed.mes.mes_api.plan.domain.ProductionPlanEntity;
import com.globalmed.mes.mes_api.plan.domain.ProductionPlanLineEntity;
import com.globalmed.mes.mes_api.plan.repository.ProductionPlanLineRepository;
import com.globalmed.mes.mes_api.plan.repository.ProductionPlanRepository;
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

    @Transactional
    public void sync() {
        OffsetDateTime cursor = cursorRepo.findById(CURSOR_KEY)
                .map(SyncCursorEntity::getLastSyncedAt)
                .orElse(OffsetDateTime.parse("1970-01-01T00:00:00Z"));

        int page = 0, size = 100;
        OffsetDateTime processedMaxTs = cursor;

        while (true) {
            List<PlanLineDto> list = client.plans(cursor, page, size);
            if (list == null || list.isEmpty()) break;

            for (PlanLineDto p : list) {
                try {
                    ensureHeader(p);
                    upsertLine(p);
                    if (p.updatedAt()!=null && p.updatedAt().isAfter(processedMaxTs)) {
                        processedMaxTs = p.updatedAt();
                    }
                } catch (Exception ex) {
                    log.warn("PLAN_SYNC_FAILED planId={}, lineNo={}, cause={}", p.planId(), p.planLineNo(), ex.getMessage());
                }
            }
            if (list.size() < size) break;
            page++;
        }

        if (processedMaxTs.isAfter(cursor)) {
            SyncCursorEntity c = cursorRepo.findById(CURSOR_KEY)
                    .orElseGet(() -> SyncCursorEntity.of(CURSOR_KEY, cursor));
            c.setLastSyncedAt(processedMaxTs);
            cursorRepo.save(c);
        }
    }

    private void ensureHeader(PlanLineDto p) {
        if (planRepo.existsById(p.planId())) return;
        java.sql.Date day = java.sql.Date.valueOf(p.dueDateUtc().atZoneSameInstant(ZoneOffset.UTC).toLocalDate());
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
        line.setUnit((p.unit()==null || p.unit().isBlank()) ? "EA" : p.unit());
        line.setDueDate(p.dueDateUtc());
        line.setPriority(p.priority());
        if (p.isDeleted()) { line.setIsDeleted(true); line.setDeletedAt(OffsetDateTime.now(ZoneOffset.UTC)); }
        else { line.setIsDeleted(false); line.setDeletedAt(null); }
        line.setModifiedBy("sync");
        line.setModifiedAt(OffsetDateTime.now(ZoneOffset.UTC));

        lineRepo.save(line);
    }
}