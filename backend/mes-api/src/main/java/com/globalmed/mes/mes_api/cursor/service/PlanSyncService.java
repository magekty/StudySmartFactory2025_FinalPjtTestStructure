// src/main/java/com/globalmed/mes/mes_api/cursor/service/PlanSyncService.java
package com.globalmed.mes.mes_api.cursor.service;

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
        // 커서(UTC)
        Instant repoCursor = cursorRepo.get(CURSOR_KEY);
        Instant nowUtc = Instant.now();

        // since 계산: 미래 클램프 + 경계 스큐(-1s) 단, EPOCH일 때는 스큐 금지
        Instant baseSince = repoCursor.isAfter(nowUtc) ? nowUtc : repoCursor;
        Instant sinceInstant = baseSince.equals(Instant.EPOCH) ? Instant.EPOCH : baseSince.minusSeconds(1);
        OffsetDateTime since = OffsetDateTime.ofInstant(sinceInstant, ZoneOffset.UTC);

        int page = 0, size = 100;
        // 처리 최대값은 "실제 커서값"에서 시작(스큐 값이 아닌)
        Instant processedMax = repoCursor;
        int fetchedTotal = 0, upserts = 0, deletes = 0;

        long auditId = audit.start("PLANS", sinceInstant, nowUtc);

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

                    if (p.updatedAt() != null) {
                        Instant u = p.updatedAt().toInstant();
                        if (u.isAfter(processedMax)) processedMax = u;
                    }
                }
                if (got < size) break;
                page++;
            }

            if (processedMax.isAfter(repoCursor)) {
                Instant saveTs = processedMax.isAfter(nowUtc) ? nowUtc : processedMax; // 미래 클램프
                cursorRepo.set(CURSOR_KEY, saveTs);                                   // UTC 저장
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