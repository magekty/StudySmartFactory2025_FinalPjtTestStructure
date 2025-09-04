// src/main/java/com/globalmed/mes/mes_api/sync/PlanSyncService.java
package com.globalmed.mes.mes_api.sync;

import com.globalmed.mes.mes_api.cursor.repository.SyncCursorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Profile("legacy")              // 기본 비활성(legacy 프로필에서만 활성)
@Service("planSyncServiceLegacy")
@RequiredArgsConstructor
public class PlanSyncService {

    private final JdbcTemplate jdbc;
    private final SyncAuditLogger audit;
    private final SyncCursorRepository cursorRepo; // 가정: 커서 조회/저장 래퍼

    public void syncPlans() {
        // 커서 → 윈도 잡기
        Instant last = cursorRepo.get("erp_plans"); // updatedSince
        Instant now  = Instant.now();

        long auditId = audit.start("PLANS", last, now);
        int fetched = 0, upserts = 0, deletes = 0;

        try {
            int page = 0, size = 500;
            boolean lastPage;
            do {
                // ERP 호출부(예시): /plans?updatedSince=last&page=size
                var pageData = fetchPlansFromErp(last, page, size); // content, last 플래그 가정
                fetched += pageData.getContent().size();

                // 업서트/삭제 카운팅(예시)
                for (var p : pageData.getContent()) {
                    if (p.isDeleted()) {
                        deletes += upsertOrDeletePlan(p); // 구현부에 맞게
                    } else {
                        upserts += upsertOrDeletePlan(p);
                    }
                }

                page++;
                lastPage = pageData.isLast();
            } while (!lastPage);

            // 커서 전진
            cursorRepo.set("erp_plans", now);

            audit.success(auditId, fetched, upserts, deletes, "OK");
        } catch (Exception e) {
            audit.fail(auditId, e.getMessage());
            throw e;
        }
    }

    // ↓ 실제 ERP 호출/업서트 구현은 기존 코드 사용
    private PageWrapper fetchPlansFromErp(Instant updatedSince, int page, int size) { /* ... */ return null; }
    private int upsertOrDeletePlan(PlanDto p) { /* ... */ return 1; }

    private static class PageWrapper {
        java.util.List<PlanDto> getContent(){ return java.util.List.of(); }
        boolean isLast(){ return true; }
    }
    private static class PlanDto { boolean isDeleted(){ return false; } }
}