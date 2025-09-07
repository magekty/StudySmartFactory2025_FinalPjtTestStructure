package com.globalmed.mes.mes_api.integration.erp.sync;

import com.globalmed.mes.mes_api.integration.erp.ErpIncrementalClient;
import com.globalmed.mes.mes_api.integration.erp.dto.*;
import com.globalmed.mes.mes_api.sync.SyncAuditLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.*;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IncrementalSyncService {

    private final ErpIncrementalClient client;
    private final JdbcTemplate jdbc;
    private final SyncAuditLogger audit;

    // ITEMS 증분
    public void syncItems() {
        OffsetDateTime cursor = getCursor("erp_items");
        OffsetDateTime since = normalizeSince(cursor);

        List<ItemDto> list = client.items(since);
        OffsetDateTime maxTs = cursor == null ? epochUtc() : cursor;

        for (ItemDto it : list) {
            if (Boolean.TRUE.equals(it.isDeleted())) {
                jdbc.update(
                        "UPDATE tb_item SET is_deleted=1, deleted_at=UTC_TIMESTAMP(), modified_by='sync', modified_at=UTC_TIMESTAMP() WHERE item_id=?",
                        it.itemId()
                );
            } else {
                String itemCode = (it.itemCode() == null || it.itemCode().isBlank()) ? it.itemId() : it.itemCode();
                String itemType = (it.itemType() == null || it.itemType().isBlank()) ? "F" : it.itemType();
                String unit     = (it.unit()     == null || it.unit().isBlank())     ? "EA" : it.unit();

                jdbc.update("""
                    INSERT INTO tb_item (item_id, item_code, item_name, unit, item_type, is_deleted, created_by, created_at)
                    VALUES (?, ?, ?, ?, ?, 0, 'sync', UTC_TIMESTAMP())
                    ON DUPLICATE KEY UPDATE
                      item_code = VALUES(item_code),
                      item_name = VALUES(item_name),
                      unit      = VALUES(unit),
                      item_type = VALUES(item_type),
                      is_deleted= 0,
                      deleted_at= NULL,
                      modified_by='sync',
                      modified_at=UTC_TIMESTAMP()
                    """,
                        it.itemId(), itemCode, it.itemId(), unit, itemType
                );
            }
            if (it.updatedAt() != null && it.updatedAt().isAfter(maxTs)) {
                maxTs = it.updatedAt();
            }
        }

        if (!list.isEmpty() && maxTs != null && (cursor == null || maxTs.isAfter(cursor))) {
            setCursor("erp_items", clampToNow(maxTs));
        }
    }

    // BOMS 증분 (Option B: alt_code/line_no 반영)
    // BOMS 증분: 내부 HOP 루프 + 감사 로그 추가
    public void syncBoms() {
        OffsetDateTime cursor = getCursor("erp_boms");

        // since 계산: UTC, 미래 클램프, EPOCH은 -1초 미적용
        OffsetDateTime nowUtcOdt = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime base = (cursor == null) ? epochUtc() : cursor.withOffsetSameInstant(ZoneOffset.UTC);
        base = base.isAfter(nowUtcOdt) ? nowUtcOdt : base;
        OffsetDateTime since = normalizeSince(cursor);

        long auditId = audit.start("BOMS", since.toInstant(), nowUtcOdt.toInstant());

        int fetchedTotal = 0, upserts = 0, deletes = 0;
        OffsetDateTime processedMax = (cursor == null) ? epochUtc() : cursor.withOffsetSameInstant(ZoneOffset.UTC);

        try {
            final int MAX_HOPS = 1000;

            for (int hop = 0; hop < MAX_HOPS; hop++) {
                List<BomHeaderDto> headers = client.boms(since);
                OffsetDateTime maxTs = (cursor == null) ? epochUtc() : cursor;
                int got = (headers == null ? 0 : headers.size());
                fetchedTotal += got;

                // 페이지 로그
                // 예: [BOM_SYNC] since=... got=...
                System.out.println("[BOM_SYNC] since=" + since + " got=" + got);

                if (got == 0) break;

                for (BomHeaderDto h : headers) {
                    // syncBoms() 내부, 헤더 루프 안에서 bom_id 결정/사용 부분 교체
                    String altCode = (h.altCode() == null || h.altCode().isBlank()) ? "STD" : h.altCode();
                    String computed = makeBomId(h.itemId(), h.revision(), altCode);
                    String bomId = resolveBomId(h.itemId(), h.revision(), altCode, computed); // ← 핵심

                    if (Boolean.TRUE.equals(h.isDeleted())) {
                        jdbc.update("UPDATE tb_bom_header SET is_deleted=1, deleted_at=UTC_TIMESTAMP(), modified_by='sync', modified_at=UTC_TIMESTAMP() WHERE bom_id=?", bomId);
                        jdbc.update("UPDATE tb_bom_line   SET is_deleted=1, deleted_at=UTC_TIMESTAMP(), modified_by='sync', modified_at=UTC_TIMESTAMP() WHERE bom_id=?", bomId);
                        deletes++;
                    } else {
                        OffsetDateTime nowUtc = OffsetDateTime.now(ZoneOffset.UTC);
                        OffsetDateTime effFrom = (h.effectiveFromUtc() != null) ? h.effectiveFromUtc() : nowUtc; // NOT NULL 보정
                        OffsetDateTime effTo   = h.effectiveToUtc();

                        // 헤더 upsert: bom_id는 위에서 확정한 값 사용
                        jdbc.update("""
                                INSERT INTO tb_bom_header (bom_id, item_id, revision, alt_code, eff_from, eff_to, is_deleted, created_by, created_at)
                                VALUES (?, ?, ?, ?, ?, ?, 0, 'sync', UTC_TIMESTAMP())
                                ON DUPLICATE KEY UPDATE
                                  item_id   = VALUES(item_id),
                                  revision  = VALUES(revision),
                                  alt_code  = VALUES(alt_code),
                                  eff_from  = VALUES(eff_from),
                                  eff_to    = VALUES(eff_to),
                                  is_deleted= 0,
                                  deleted_at= NULL,
                                  modified_by='sync',
                                  modified_at=UTC_TIMESTAMP()
                                """,
                                bomId, h.itemId(), h.revision(), altCode, ts(effFrom), ts(effTo)
                        );
                        upserts++;

                        // 라인 전량 교체: 같은 bomId로 삭제 후 재삽입
                        jdbc.update("DELETE FROM tb_bom_line WHERE bom_id=?", bomId);

                        if (h.lines() != null && !h.lines().isEmpty()) {
                            for (int i = 0; i < h.lines().size(); i++) {
                                BomLineDto l = h.lines().get(i);

                                Integer lineNo = (l.lineNo() != null ? l.lineNo() : (i + 1)); // 폴백
                                if (lineNo <= 0) throw new IllegalStateException("BOM_LINE_NO_INVALID: bomId=" + bomId + ", idx=" + i);

                                String componentItemId = findItemIdByIdOrCode(reqNonBlank(l.componentId(), "componentId"));

                                jdbc.update("""
                                        INSERT INTO tb_bom_line (bom_id, line_no, component_id, qty, uom, scrap_rate, is_deleted, created_by, created_at)
                                        VALUES (?, ?, ?, ?, ?, ?, 0, 'sync', UTC_TIMESTAMP())
                                        ON DUPLICATE KEY UPDATE
                                          qty        = VALUES(qty),
                                          uom        = VALUES(uom),
                                          scrap_rate = VALUES(scrap_rate),
                                          is_deleted = 0,
                                          deleted_at = NULL,
                                          modified_by= 'sync',
                                          modified_at= UTC_TIMESTAMP()
                                        """,
                                        bomId,
                                        lineNo,
                                        componentItemId,
                                        l.qty() == null ? 0 : l.qty(),
                                        (l.uom() == null || l.uom().isBlank()) ? "EA" : l.uom(),
                                        l.scrapRate() == null ? 0 : l.scrapRate()
                                );
                                upserts++;
                            }
                        }
                    }

                    // processedMax 업데이트(헤더/라인의 updatedAt 최대)
                    if (h.updatedAt() != null && (maxTs == null || h.updatedAt().isAfter(maxTs))) maxTs = h.updatedAt();
                    if (h.lines() != null) {
                        for (BomLineDto l : h.lines()) {
                            if (l.updatedAt() != null && (maxTs == null || l.updatedAt().isAfter(maxTs))) maxTs = l.updatedAt();
                        }
                    }
                }
                if (!headers.isEmpty() && maxTs != null && (cursor == null || maxTs.isAfter(cursor))) {
                    setCursor("erp_boms", clampToNow(maxTs));
                }
                // 다음 HOP: processedMax가 증가했을 때만 전진
                OffsetDateTime nextSince = processedMax.plusNanos(1);
                if (!nextSince.isAfter(since)) break; // 진행 불가(무한루프 방지)
                since = nextSince;
            }

            // 커서 저장(미래 클램프)
            OffsetDateTime nowClamp = OffsetDateTime.now(ZoneOffset.UTC);
            if (processedMax != null && (cursor == null || processedMax.isAfter(cursor))) {
                OffsetDateTime saveTs = processedMax.isAfter(nowClamp) ? nowClamp : processedMax;
                setCursor("erp_boms", saveTs);
                System.out.println("[BOM_SYNC] done fetched=" + fetchedTotal + " upserts=" + upserts + " deletes=" + deletes + " newCursor=" + saveTs);
            } else {
                System.out.println("[BOM_SYNC] done fetched=" + fetchedTotal + " upserts=" + upserts + " deletes=" + deletes + " cursorUnchanged=" + cursor);
            }

            audit.success(auditId, fetchedTotal, upserts, deletes, "OK");
        } catch (Exception e) {
            audit.fail(auditId, e.toString());
            throw e;
        }
    }

    // ===== 공통 유틸 =====
    private String reqNonBlank(String v, String name) {
        if (v == null || v.isBlank()) throw new IllegalStateException("BOM_" + name.toUpperCase() + "_EMPTY");
        return v;
    }

    private String makeBomId(String itemId, String revision, String altCode) {
        String canonical = String.join("|", itemId, revision, (altCode == null ? "STD" : altCode));
        String candidate = "BOM-" + canonical;
        if (candidate.length() <= 50) return candidate;
        try {
            var md = java.security.MessageDigest.getInstance("SHA-1");
            String hex = java.util.HexFormat.of().formatHex(md.digest(canonical.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
            return "BOM-" + hex.substring(0, 40);
        } catch (Exception e) {
            String fb = "BOM-" + Math.abs(canonical.hashCode());
            return fb.substring(0, Math.min(50, fb.length()));
        }
    }
    private OffsetDateTime normalizeSince(OffsetDateTime cursor) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime base = (cursor == null) ? epochUtc() : cursor.withOffsetSameInstant(ZoneOffset.UTC);
        base = base.isAfter(now) ? now : base;
        if (base.toInstant().equals(Instant.EPOCH)) return base; // EPOCH는 -1초 스큐 미적용
        return base.minusSeconds(1);
    }

    private OffsetDateTime clampToNow(OffsetDateTime ts) {
        if (ts == null) return epochUtc();
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        return ts.isAfter(now) ? now : ts.withOffsetSameInstant(ZoneOffset.UTC);
    }

    private OffsetDateTime epochUtc() {
        return OffsetDateTime.ofInstant(Instant.EPOCH, ZoneOffset.UTC);
    }

    private Timestamp ts(OffsetDateTime odt) {
        return odt == null ? null : Timestamp.from(odt.withOffsetSameInstant(ZoneOffset.UTC).toInstant());
    }

    private String findItemIdByIdOrCode(String candidate) {
        if (candidate == null || candidate.isBlank()) {
            throw new IllegalStateException("BOM_COMPONENT_ID_EMPTY");
        }
        try {
            return jdbc.queryForObject(
                    "SELECT item_id FROM tb_item WHERE item_id=? LIMIT 1",
                    String.class, candidate
            );
        } catch (EmptyResultDataAccessException e1) {
            try {
                return jdbc.queryForObject(
                        "SELECT item_id FROM tb_item WHERE item_code=? LIMIT 1",
                        String.class, candidate
                );
            } catch (EmptyResultDataAccessException e2) {
                throw new IllegalStateException("BOM_COMPONENT_NOT_FOUND:" + candidate);
            }
        }
    }
    private OffsetDateTime getCursor(String key) {
        return jdbc.query("""
      SELECT last_synced_at FROM tb_sync_cursor WHERE cursor_key=?
    """, rs -> rs.next() ? rs.getTimestamp(1).toInstant().atOffset(ZoneOffset.UTC) : OffsetDateTime.parse("1970-01-01T00:00:00Z"), key);
    }

    private void setCursor(String key, OffsetDateTime odt) {
        jdbc.update("""
      INSERT INTO tb_sync_cursor (cursor_key, last_synced_at)
      VALUES (?, ?) ON DUPLICATE KEY UPDATE last_synced_at=VALUES(last_synced_at)
    """, key, Timestamp.from(odt.toInstant()));
    }

    // 자연키로 기존 bom_id 조회 → 있으면 그대로 사용, 없으면 새로 생성
    private String resolveBomId(String itemId, String revision, String altCode, String computedBomId) {
        try {
            // 기존 행의 bom_id 우선 사용
            return jdbc.queryForObject(
                    "SELECT bom_id FROM tb_bom_header WHERE item_id=? AND revision=? AND alt_code=? LIMIT 1",
                    String.class, itemId, revision, altCode
            );
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return computedBomId; // 없으면 새 규칙으로 생성한 bom_id 사용
        }
    }
}