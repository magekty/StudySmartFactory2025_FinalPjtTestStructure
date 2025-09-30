// src/main/java/com/globalmed/mes/mes_api/cursor/api/PlanSyncAdminController.java
package com.globalmed.mes.mes_api.cursor.api;

import com.globalmed.mes.mes_api.cursor.domain.SyncCursorEntity;
import com.globalmed.mes.mes_api.cursor.repository.SyncCursorRepository;
import com.globalmed.mes.mes_api.cursor.service.PlanSyncService;
import com.globalmed.mes.mes_api.integration.erp.ErpIncrementalClient;
import com.globalmed.mes.mes_api.integration.erp.dto.PlanLineDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

@RestController
@RequestMapping("/internal/sync/plans")
@RequiredArgsConstructor
public class PlanSyncAdminController {

    private final PlanSyncService planSyncService;
    private final ErpIncrementalClient erp;
    private final SyncCursorRepository cursorRepo;
    private final JdbcTemplate jdbc;

    @GetMapping("/cursor")
    public Map<String, Object> cursor() {
        var map = new LinkedHashMap<String, Object>();
        var cur = cursorRepo.findById("erp_plans")
                .map(SyncCursorEntity::getLastSyncedAt) // LocalDateTime
                .map(dt -> dt.atOffset(ZoneOffset.UTC))
                .orElse(Instant.EPOCH.atOffset(ZoneOffset.UTC));
        map.put("cursor", cur.toString());
        return map;
    }

    @PostMapping("/run")
    public Map<String, Object> runOnce() {
        var res = new LinkedHashMap<String, Object>();
        var before = countAndMax();
        planSyncService.sync(); // 동기 실행
        var after = countAndMax();

        res.put("before", before);
        res.put("after", after);
        res.put("cursor", cursorRepo.findById("erp_plans")
                .map(SyncCursorEntity::getLastSyncedAt)
                .map(dt -> dt.atOffset(ZoneOffset.UTC).toString())
                .orElse(Instant.EPOCH.atOffset(ZoneOffset.UTC).toString()));
        return res;
    }

    @GetMapping("/ping")
    public Map<String, Object> pingErp(
            @RequestParam(defaultValue = "1970-01-01T00:00:00Z") String since,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "1") int size
    ) {
        var res = new LinkedHashMap<String, Object>();
        OffsetDateTime odt = OffsetDateTime.parse(since);
        List<PlanLineDto> list = erp.plans(odt, page, size);
        res.put("ok", true);
        res.put("requestedSince", odt.toString());
        res.put("received", (list == null ? 0 : list.size()));
        if (list != null && !list.isEmpty()) {
            var first = list.get(0);
            res.put("sample", Map.of(
                    "planId", first.planId(),
                    "lineNo", first.planLineNo(),
                    "updatedAt", String.valueOf(first.updatedAt())
            ));
        }
        return res;
    }

    private Map<String, Object> countAndMax() {
        var out = new LinkedHashMap<String, Object>();
        Integer cnt = jdbc.queryForObject(
                "SELECT COUNT(*) FROM tb_production_plan_line", Integer.class);
        String maxMod = jdbc.queryForObject(
                "SELECT DATE_FORMAT(MAX(modified_at), '%Y-%m-%dT%H:%i:%sZ') FROM tb_production_plan_line",
                String.class);
        out.put("lines", cnt == null ? 0 : cnt);
        out.put("maxModifiedAt", maxMod);
        return out;
    }
}