// src/main/java/com/demo/erp/web/mock/MockBomStore.java
package com.demo.erp.web.mock;

import com.demo.erp.web.dto.BomHeaderDto;
import com.demo.erp.web.dto.BomLineDto;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
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

    public synchronized void seedIfEmpty() {
        if (!latest.isEmpty()) return;
        seedOne("ITEM-100","A");
        seedOne("ITEM-200","A");
        seedOne("ITEM-300","A");
    }
    public synchronized void addOrReplace(String itemId, String rev, String altCode, OffsetDateTime ts) {
        List<BomLineDto> lines = List.of(
                new BomLineDto(1, itemId, 1.0, "EA", 0.0, ts, false),
                new BomLineDto(2, itemId, 0.5, "EA", 0.0, ts, false)
        );

        // 이 부분은 기존과 동일하며, BomHeaderDto에 lines 리스트를 전달
        latest.put(key(itemId, rev, altCode),
                new BomHeaderDto(
                        // bomId는 itemId, rev, altCode 조합으로 생성
                        key(itemId, rev, altCode),
                        itemId,
                        rev,
                        ts,          // effFrom
                        null,        // effTo
                        ts,          // updatedAt
                        false,       // isDeleted
                        lines        // lines
                )
        );
    }

    private void seedOne(String itemId, String revision) {
        var base = nowUtc().minusMinutes(30);
        String bomId = computeBomId(itemId, revision);
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

    public synchronized List<BomHeaderDto> listSince(OffsetDateTime since, int maxRows) {
        var s = since == null ? OffsetDateTime.MIN : since.withOffsetSameInstant(ZoneOffset.UTC);
        return latest.values().stream()
                .filter(h -> h.updatedAt() != null && (h.updatedAt().isAfter(s) || h.updatedAt().isEqual(s)))
                .sorted(Comparator
                        .comparing(BomHeaderDto::updatedAt)
                        .thenComparing(BomHeaderDto::bomId))
                .limit(Math.max(1, maxRows))
                .toList();
    }
    public synchronized void deleteHeader(String bomId) {
        var cur = latest.get(bomId);
        if (cur == null) throw new NoSuchElementException("BOM_HEADER_NOT_FOUND: " + bomId);
        var n = nowUtc();
        // 절대 latest.remove 하지 말 것!
        latest.put(bomId, new BomHeaderDto(
                cur.bomId(), cur.itemId(), cur.revision(),
                cur.effFrom(), cur.effTo(),
                n, true, cur.lines()  // ← tombstone 노출
        ));
    }

    public synchronized void setLineComponent(String bomId, int lineNo, String newComponentId) {
        var cur = latest.get(bomId);
        if (cur == null) throw new NoSuchElementException("BOM_HEADER_NOT_FOUND: " + bomId);
        var n = nowUtc();
        boolean found = false;
        var newLines = new java.util.ArrayList<com.demo.erp.web.dto.BomLineDto>();
        for (var l : cur.lines()) {
            if (java.util.Objects.equals(l.lineNo(), lineNo)) {
                found = true;
                newLines.add(new com.demo.erp.web.dto.BomLineDto(
                        l.lineNo(), newComponentId, l.qty(), l.uom(), l.scrapRate(), n, l.isDeleted()
                ));
            } else {
                newLines.add(l);
            }
        }
        if (!found) throw new NoSuchElementException("BOM_LINE_NOT_FOUND: " + bomId + " #" + lineNo);
        // 헤더 updatedAt도 라인 변경 시 now로 끌어올림(증분 안전)
        latest.put(bomId, new BomHeaderDto(
                cur.bomId(), cur.itemId(), cur.revision(),
                cur.effFrom(), cur.effTo(),
                n, cur.isDeleted(),
                newLines
        ));
    }
}