// src/main/java/com/demo/erp/web/mock/MockErpController.java
package com.demo.erp.web.mock;

import com.demo.erp.web.dto.BomHeaderDto;
import com.demo.erp.web.dto.ItemDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

@Profile("dev")
@RestController
@RequestMapping("/mock")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "mock", name = "enabled", havingValue = "true")
@Slf4j
public class MockErpController {

    private final MockItemStore itemStore;
    private final MockBomStore bomStore;

    @Value("${mock.items.max-rows:100}")
    private int itemsMaxRows;

    @Value("${mock.boms.max-rows:2}")
    private int bomsMaxRows;

    // ITEMS
    @GetMapping("/items")
    public ResponseEntity<List<ItemDto>> items(@RequestParam(required = false) String updatedSince) {
        try {
            itemStore.seedIfEmpty();
            OffsetDateTime since = parse(updatedSince);
            var out = itemStore.listSince(since, itemsMaxRows);
            log.info("[MOCK-ERP] GET /mock/items since={} -> {}", since, out.size());
            return ResponseEntity.ok(out);
        } catch (Exception e) {
            log.error("[MOCK-ERP] /mock/items failed", e);
            return ResponseEntity.ok(List.of()); // 테스트 편의: 500 대신 빈 배열
        }
    }

    // BOMS
    @GetMapping("/boms")
    public ResponseEntity<List<BomHeaderDto>> boms(@RequestParam(required = false) String updatedSince) {
        try {
            bomStore.seedIfEmpty();
            OffsetDateTime since = parse(updatedSince);
            var out = bomStore.listSince(since, bomsMaxRows);
            log.info("[MOCK-ERP] GET /mock/boms since={} -> {}", since, out.size());
            return ResponseEntity.ok(out);
        } catch (Exception e) {
            log.error("[MOCK-ERP] /mock/boms failed", e);
            return ResponseEntity.ok(List.of()); // 테스트 편의
        }
    }

    // DEV 시드/터치
    @PostMapping("/dev/items/seed")
    public String seedItems() { itemStore.seedIfEmpty(); return "OK"; }

    @PostMapping("/dev/items/touch")
    public String touchItem(@RequestParam String itemId,
                            @RequestParam(required = false) String unit,
                            @RequestParam(required = false) String itemType,
                            @RequestParam(required = false) Boolean isDeleted) {
        if (itemId == null || itemId.isBlank()) throw new IllegalArgumentException("itemId empty");
        itemStore.touch(itemId, unit, itemType, isDeleted);
        return "OK";
    }

    @PostMapping("/dev/boms/seed")
    public String seedBoms() { bomStore.seedIfEmpty(); return "OK"; }

    @PostMapping("/dev/boms/touch-header")
    public String touchBomHeader(@RequestParam String bomId) {
        if (bomId == null || bomId.isBlank()) throw new IllegalArgumentException("bomId empty");
        bomStore.touchHeader(bomId);
        return "OK";
    }

    @PostMapping("/dev/boms/touch-line")
    public String touchBomLine(@RequestParam String bomId,
                               @RequestParam Integer lineNo,
                               @RequestParam(required = false) Double deltaQty) {
        if (bomId == null || bomId.isBlank()) throw new IllegalArgumentException("bomId empty");
        if (lineNo == null || lineNo <= 0) throw new IllegalArgumentException("lineNo invalid");
        bomStore.touchLine(bomId, lineNo, deltaQty);
        return "OK";
    }

    // 삭제/Unknown component/대량 시드 (경로 앞에 /mock 중복 제거)
    @PostMapping("/dev/boms/delete-header")
    public String deleteBomHeader(@RequestParam String bomId) {
        if (bomId == null || bomId.isBlank()) throw new IllegalArgumentException("bomId empty");
        bomStore.deleteHeader(bomId);
        return "OK";
    }

    @PostMapping("/dev/boms/set-line-component")
    public String setLineComponent(@RequestParam String bomId,
                                   @RequestParam Integer lineNo,
                                   @RequestParam String componentId) {
        if (bomId == null || bomId.isBlank()) throw new IllegalArgumentException("bomId empty");
        if (lineNo == null || lineNo <= 0) throw new IllegalArgumentException("lineNo invalid");
        if (componentId == null || componentId.isBlank()) throw new IllegalArgumentException("componentId empty");
        bomStore.setLineComponent(bomId, lineNo, componentId);
        return "OK";
    }

    @PostMapping("/dev/boms/seed-bulk")
    public String seedBomBulk(@RequestParam(defaultValue = "50") Integer count) {
        int c = (count == null || count < 1) ? 1 : Math.min(count, 1000);
        var base = OffsetDateTime.now().minusMinutes(20);
        for (int i = 1; i <= c; i++) {
            String itemId = "ITEM-" + (1000 + i);
            // addOrReplace(itemId, revision, effFrom/updatedAt 기준 시각)
            bomStore.addOrReplace(itemId, "A", "", base); // ← 빈 문자열 파라미터 제거(시그니처 일치)
        }
        return "OK";
    }

    private OffsetDateTime parse(String s) {
        if (s == null || s.isBlank()) return null;
        try { return OffsetDateTime.parse(s); }
        catch (DateTimeParseException e) { return null; }
    }
}