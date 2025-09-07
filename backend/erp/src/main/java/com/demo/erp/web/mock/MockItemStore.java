// src/main/java/com/demo/erp/web/mock/MockItemStore.java
package com.demo.erp.web.mock;

import com.demo.erp.web.dto.ItemDto;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MockItemStore {
    private final Map<String, ItemDto> latest = new ConcurrentHashMap<>();

    private OffsetDateTime nowUtc() { return OffsetDateTime.now(ZoneOffset.UTC); }

    public synchronized void seedIfEmpty() {
        if (!latest.isEmpty()) return;
        var t = nowUtc().minusMinutes(30);
        latest.put("ITEM-100", new ItemDto("ITEM-100","ITEM-100","EA","F", t, false));
        latest.put("ITEM-200", new ItemDto("ITEM-200","ITEM-200","EA","F", t, false));
        latest.put("ITEM-300", new ItemDto("ITEM-300","ITEM-300","EA","F", t, false));
    }

    public synchronized void touch(String itemId, String unit, String itemType, Boolean isDeleted) {
        if (itemId == null || itemId.isBlank()) throw new IllegalArgumentException("ITEM_ID_EMPTY");
        ItemDto cur = latest.get(itemId);
        var n = nowUtc();
        if (cur == null) {
            latest.put(itemId, new ItemDto(
                    itemId, itemId,
                    unit == null ? "EA" : unit,
                    itemType == null ? "F" : itemType,
                    n,
                    isDeleted != null && isDeleted
            ));
        } else {
            latest.put(itemId, new ItemDto(
                    cur.itemId(),
                    cur.itemCode(),
                    unit == null ? cur.unit() : unit,
                    itemType == null ? cur.itemType() : itemType,
                    n,
                    isDeleted == null ? cur.isDeleted() : isDeleted
            ));
        }
    }

    public synchronized List<ItemDto> listSince(OffsetDateTime since, int maxRows) {
        var s = since == null ? OffsetDateTime.MIN : since.withOffsetSameInstant(ZoneOffset.UTC);
        return latest.values().stream()
                .filter(i -> i.updatedAt() != null && (i.updatedAt().isAfter(s) || i.updatedAt().isEqual(s)))
                .sorted(Comparator.comparing(ItemDto::updatedAt).thenComparing(ItemDto::itemId))
                .limit(Math.max(1, maxRows))
                .toList();
    }
}