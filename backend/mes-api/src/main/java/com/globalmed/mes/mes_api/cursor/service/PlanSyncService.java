package com.globalmed.mes.mes_api.cursor.service;

import com.globalmed.mes.mes_api.cursor.repository.SyncCursorRepository;
import com.globalmed.mes.mes_api.cursor.domain.SyncCursor;
import com.globalmed.mes.mes_api.integration.erp.ErpIncrementalClient;
import com.globalmed.mes.mes_api.integration.erp.dto.PlanLineDto;
import com.globalmed.mes.mes_api.plan.domain.ProductionPlanLine;
import com.globalmed.mes.mes_api.plan.repository.ProductionPlanLineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlanSyncService {

    private static final String CURSOR_KEY = "erp_plans";
    private final ErpIncrementalClient client;
    private final ProductionPlanLineRepository repo;
    private final SyncCursorRepository cursorRepo;

    @Transactional
    public void sync() {
        OffsetDateTime cursor = cursorRepo.findById(CURSOR_KEY)
                .map(SyncCursor::getLastSyncedAt)
                .orElse(OffsetDateTime.parse("1970-01-01T00:00:00Z"));

        int page = 0, size = 100;
        OffsetDateTime maxTs = cursor;
        while (true) {
            List<PlanLineDto> list = client.plans(cursor, page, size);
            if (list == null || list.isEmpty()) break;

            for (PlanLineDto p : list) {
                maxTs = (p.updatedAt() != null && p.updatedAt().isAfter(maxTs)) ? p.updatedAt() : maxTs;
                upsertLine(p);
            }
            if (list.size() < size) break;
            page++;
        }

        if (maxTs.isAfter(cursor)) {
            SyncCursor c = cursorRepo.findById(CURSOR_KEY)
                    .orElseGet(() -> SyncCursor.of(CURSOR_KEY, cursor));
            c.setLastSyncedAt(maxTs);
            cursorRepo.save(c);
            c.setCursorKey(CURSOR_KEY);
            c.setLastSyncedAt(maxTs);
            cursorRepo.save(c);
        }
    }

    private void upsertLine(PlanLineDto p) {
        ProductionPlanLine line = repo.findByPlanIdAndPlanLineNo(p.planId(), p.planLineNo())
                .orElseGet(ProductionPlanLine::new);

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
        if (p.isDeleted()) {
            line.setIsDeleted(true);
            line.setDeletedAt(OffsetDateTime.now(ZoneOffset.UTC));
        } else {
            line.setIsDeleted(false);
            line.setDeletedAt(null);
        }
        line.setModifiedBy("sync");
        line.setModifiedAt(OffsetDateTime.now(ZoneOffset.UTC));

        repo.save(line);
    }
}