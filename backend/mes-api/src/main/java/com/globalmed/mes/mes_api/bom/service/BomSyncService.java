// src/main/java/com/globalmed/mes/mes_api/bom/service/BomSyncService.java
package com.globalmed.mes.mes_api.bom.service;

import com.globalmed.mes.mes_api.bom.domain.BomHeaderEntity;
import com.globalmed.mes.mes_api.bom.domain.BomLineEntity;
import com.globalmed.mes.mes_api.bom.repository.BomHeaderRepository;
import com.globalmed.mes.mes_api.bom.repository.BomLineRepository;
import com.globalmed.mes.mes_api.cursor.repository.SyncCursorRepository;
import com.globalmed.mes.mes_api.integration.erp.ErpIncrementalClient;
import com.globalmed.mes.mes_api.integration.erp.dto.BomHeaderDto;
import com.globalmed.mes.mes_api.integration.erp.dto.BomLineDto;
import com.globalmed.mes.mes_api.sync.SyncAuditLogger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HexFormat;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BomSyncService {

    private static final String CURSOR_KEY = "erp_boms";

    private final ErpIncrementalClient client;
    private final BomHeaderRepository headerRepo;
    private final BomLineRepository lineRepo;
    private final SyncCursorRepository cursorRepo;
    private final SyncAuditLogger audit;

    @Transactional
    public void sync() {
        Instant repoCursor = cursorRepo.get(CURSOR_KEY);
        Instant nowUtc = Instant.now();

        Instant baseSince = repoCursor.isAfter(nowUtc) ? nowUtc : repoCursor;
        Instant sinceInstant = baseSince.equals(Instant.EPOCH) ? Instant.EPOCH : baseSince.minusSeconds(1);
        OffsetDateTime pageSince = OffsetDateTime.ofInstant(sinceInstant, ZoneOffset.UTC);

        long auditId = audit.start("BOMS", sinceInstant, nowUtc);

        int fetchedTotal = 0, upserts = 0, deletes = 0;
        Instant processedMax = repoCursor;

        try {
            for (int hop = 0; hop < 1000; hop++) { // 안전 가드
                List<BomHeaderDto> list = client.boms(pageSince);
                int got = (list == null ? 0 : list.size());
                fetchedTotal += got;
                log.info("[BOM_SYNC] since={} got={}", pageSince, got);
                if (got == 0) break;

                for (BomHeaderDto h : list) {
                    String itemId = h.itemId();
                    String altCode = (h.altCode() == null || h.altCode().isBlank()) ? "STD" : h.altCode();
                    String bomId = makeBomId(itemId, h.revision(), altCode);

                    OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

                    // 헤더 업서트
                    BomHeaderEntity header = headerRepo.findById(bomId)
                            .orElseGet(() -> BomHeaderEntity.builder()
                                    .bomId(bomId)
                                    .itemId(itemId)
                                    .revision(h.revision())
                                    .altCode(altCode)
                                    .createdBy("sync")
                                    .createdAt(now)
                                    .build()
                            );

                    header.setDescription(h.description());
                    header.setEffFromUtc(h.effectiveFromUtc() != null ? h.effectiveFromUtc().withOffsetSameInstant(ZoneOffset.UTC) : now);
                    header.setEffToUtc(h.effectiveToUtc() != null ? h.effectiveToUtc().withOffsetSameInstant(ZoneOffset.UTC) : null);
                    boolean headerDeleted = Boolean.TRUE.equals(h.isDeleted());
                    header.setIsDeleted(headerDeleted);
                    header.setDeletedAt(headerDeleted ? now : null);
                    header.setModifiedBy("sync");
                    header.setModifiedAt(now);

                    headerRepo.save(header);
                    if (headerDeleted) deletes++; else upserts++;

                    // 라인 업서트
                    if (h.lines() != null) {
                        for (BomLineDto l : h.lines()) {
                            BomLineEntity line = lineRepo.findByHeader_BomIdAndLineNo(bomId, l.lineNo())
                                    .orElseGet(() -> BomLineEntity.builder()
                                            .header(header)
                                            .lineNo(l.lineNo())
                                            .createdBy("sync")
                                            .createdAt(now)
                                            .build()
                                    );

                            line.setComponentId(l.componentId());
                            line.setQty(nullSafe(l.qty()));
                            line.setUom((l.uom() == null || l.uom().isBlank()) ? "EA" : l.uom());
                            line.setScrapRate(nullSafe(l.scrapRate()));
                            boolean lineDeleted = Boolean.TRUE.equals(l.isDeleted());
                            line.setIsDeleted(lineDeleted);
                            line.setDeletedAt(lineDeleted ? now : null);
                            line.setModifiedBy("sync");
                            line.setModifiedAt(now);

                            lineRepo.save(line);
                            if (lineDeleted) deletes++; else upserts++;
                        }
                    }

                    // processedMax = 헤더/라인 updatedAt의 최대
                    if (h.updatedAt() != null && h.updatedAt().toInstant().isAfter(processedMax)) {
                        processedMax = h.updatedAt().toInstant();
                    }
                    if (h.lines() != null) {
                        for (BomLineDto l : h.lines()) {
                            if (l.updatedAt() != null && l.updatedAt().toInstant().isAfter(processedMax)) {
                                processedMax = l.updatedAt().toInstant();
                            }
                        }
                    }
                }

                // 다음 홉: 내부 페이지네이션 (ERP maxRows 대비)
                Instant nextSince = processedMax.isAfter(Instant.EPOCH)
                        ? processedMax.plusNanos(1)
                        : pageSince.toInstant().plusSeconds(1);
                if (!nextSince.isAfter(pageSince.toInstant())) break; // 진행 불가 시 종료
                pageSince = OffsetDateTime.ofInstant(nextSince, ZoneOffset.UTC);
            }

            // 커서 저장
            if (processedMax.isAfter(repoCursor)) {
                Instant saveTs = processedMax.isAfter(nowUtc) ? nowUtc : processedMax;
                cursorRepo.set(CURSOR_KEY, saveTs);
                log.info("[BOM_SYNC] done fetched={} upserts={} deletes={} newCursor={}", fetchedTotal, upserts, deletes, saveTs);
            } else {
                log.info("[BOM_SYNC] done fetched={} upserts={} deletes={} cursorUnchanged={}", fetchedTotal, upserts, deletes, repoCursor);
            }

            audit.success(auditId, fetchedTotal, upserts, deletes, "OK");
        } catch (Exception e) {
            audit.fail(auditId, e.toString());
            throw e;
        }
    }

    private static BigDecimal nullSafe(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private static String makeBomId(String itemId, String revision, String altCode) {
        String canonical = String.join("|", itemId, revision, altCode == null ? "STD" : altCode);
        String candidate = "BOM-" + canonical;
        if (candidate.length() <= 50) return candidate;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(canonical.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            String hex = HexFormat.of().formatHex(digest);
            return "BOM-" + hex.substring(0, 40); // 총 44자
        } catch (Exception e) {
            // fallback
            return ("BOM-" + Math.abs(canonical.hashCode())).substring(0, Math.min(50, ("BOM-" + Math.abs(canonical.hashCode())).length()));
        }
    }
}