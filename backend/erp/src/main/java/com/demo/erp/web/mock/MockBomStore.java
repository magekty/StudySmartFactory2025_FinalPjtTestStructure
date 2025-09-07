// src/main/java/com/demo/erp/web/mock/MockBomStore.java
package com.demo.erp.web.mock;

import com.demo.erp.web.dto.BomHeaderDto;
import com.demo.erp.web.dto.BomLineDto;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MockBomStore {

    // key: bomId
    private final Map<String, BomHeaderDto> latest = new ConcurrentHashMap<>();
    private String key(String itemId, String rev, String alt) {
        return String.join("|", itemId, rev, (alt==null||alt.isBlank())?"STD":alt);
    }
    private OffsetDateTime nowUtc() { return OffsetDateTime.now(ZoneOffset.UTC); }

    private String computeBomId(String itemId, String revision) {
        String candidate = "BOM-" + itemId + "|" + revision;
        return candidate.length() <= 50 ? candidate : candidate.substring(0, 50);
    }

    // MockBomStore.java (핵심만)
    public synchronized void seedIfEmpty() {
        if (!latest.isEmpty()) return;
        seedOne("ITEM-100", "A", "STD");
        seedOne("ITEM-200", "A", "STD");
        seedOne("ITEM-300", "A", "STD");
        seedOne("ITEM-1003", "A", "STD"); // ← A|STD 금지
    }

    private void seedOne(String itemId, String revision, String alt) {
        var base = nowUtc().minusMinutes(30);
        String bomId = stdBomId(itemId, revision, alt); // "BOM-ITEM|REV|ALT"
        List<BomLineDto> lines = List.of(
                new BomLineDto(1, itemId, 1.0, "EA", 0.0, base, false),
                new BomLineDto(2, itemId, 0.5, "EA", 0.0, base, false)
        );
        latest.put(bomId, new BomHeaderDto(
                bomId, itemId, revision,
                base, null,
                base, false,
                lines
        ));
    }

    // 표준 BOM ID 빌더 + 입력 정규화
    private String stdBomId(String itemId, String rev, String alt) {
        String a = (alt == null || alt.isBlank()) ? "STD" : alt;
        return "BOM-" + itemId + "|" + rev + "|" + a;
    }
    private String normalizeBomId(String bomId) {
        if (bomId == null || bomId.isBlank()) throw new IllegalArgumentException("bomId empty");
        String raw = bomId.startsWith("BOM-") ? bomId.substring(4) : bomId;
        String[] p = raw.split("\\|"); // ITEM | REV | ALT?
        if (p.length < 2 || p.length > 3) throw new IllegalArgumentException("bomId format invalid: " + bomId);
        return stdBomId(p[0], p[1], p.length == 3 ? p[2] : "STD");
    }

    public synchronized void touchHeader(String bomId) {
        var cur = latest.get(bomId);
        if (cur == null) throw new NoSuchElementException("BOM_HEADER_NOT_FOUND");
        var n = nowUtc();
        latest.put(bomId, new BomHeaderDto(
                cur.bomId(), cur.itemId(), cur.revision(),
                cur.effFrom(), cur.effTo(),
                n, cur.isDeleted(),
                cur.lines()
        ));
    }

    public synchronized void touchLine(String bomId, int lineNo, Double deltaQty) {
        var cur = latest.get(bomId);
        if (cur == null) throw new NoSuchElementException("BOM_HEADER_NOT_FOUND");
        var n = nowUtc();
        double delta = deltaQty == null ? 0.0 : deltaQty;
        List<BomLineDto> newLines = new ArrayList<>();
        for (BomLineDto l : cur.lines()) {
            if (Objects.equals(l.lineNo(), lineNo)) {
                double newQty = (l.qty() == null ? 0.0 : l.qty()) + delta;
                newLines.add(new BomLineDto(l.lineNo(), l.componentId(), newQty, l.uom(), l.scrapRate(), n, l.isDeleted()));
            } else {
                newLines.add(l);
            }
        }
        latest.put(bomId, new BomHeaderDto(
                cur.bomId(), cur.itemId(), cur.revision(),
                cur.effFrom(), cur.effTo(),
                n, cur.isDeleted(),
                newLines
        ));
    }


    public synchronized void addOrReplace(String itemId, String rev, String alt, OffsetDateTime ts) {
        String bomId = stdBomId(itemId, rev, alt);
        List<BomLineDto> lines = List.of(
                new BomLineDto(1, itemId, 1.0, "EA", 0.0, ts, false),
                new BomLineDto(2, itemId, 0.5, "EA", 0.0, ts, false)
        );
        latest.put(bomId, new BomHeaderDto(bomId, itemId, rev, ts, null, ts, false, lines));
    }

    public synchronized void deleteHeader(String bomIdInput) {
        String bomId = normalizeBomId(bomIdInput);
        BomHeaderDto cur = latest.get(bomId);
        if (cur == null) throw new NoSuchElementException("BOM_HEADER_NOT_FOUND: " + bomId);
        OffsetDateTime n = nowUtc();
        latest.put(bomId, new BomHeaderDto(
                cur.bomId(), cur.itemId(), cur.revision(),
                cur.effFrom(), cur.effTo(),
                n, true, cur.lines() // tombstone
        ));
    }

    public synchronized void setLineComponent(String bomIdInput, int lineNo, String componentId) {
        String bomId = normalizeBomId(bomIdInput);
        BomHeaderDto cur = latest.get(bomId);
        if (cur == null) throw new NoSuchElementException("BOM_HEADER_NOT_FOUND: " + bomId);
        OffsetDateTime n = nowUtc();
        boolean found = false;
        List<BomLineDto> newLines = new java.util.ArrayList<>();
        for (var l : cur.lines()) {
            if (java.util.Objects.equals(l.lineNo(), lineNo)) {
                found = true;
                newLines.add(new BomLineDto(l.lineNo(), componentId, l.qty(), l.uom(), l.scrapRate(), n, l.isDeleted()));
            } else newLines.add(l);
        }
        if (!found) throw new NoSuchElementException("BOM_LINE_NOT_FOUND: " + bomId + " #" + lineNo);
        latest.put(bomId, new BomHeaderDto(
                cur.bomId(), cur.itemId(), cur.revision(),
                cur.effFrom(), cur.effTo(),
                n, cur.isDeleted(), newLines
        ));
    }

    public synchronized List<BomHeaderDto> listSince(OffsetDateTime since, int maxRows) {
        OffsetDateTime s = since == null ? OffsetDateTime.MIN : since.withOffsetSameInstant(ZoneOffset.UTC);
        return latest.values().stream()
                .filter(h -> h.updatedAt() != null && (h.updatedAt().isAfter(s) || h.updatedAt().isEqual(s)))
                .sorted(java.util.Comparator.comparing(BomHeaderDto::updatedAt).thenComparing(BomHeaderDto::bomId))
                .limit(Math.max(1, maxRows))
                .toList();
    }
}