// src/main/java/com/demo/erp/service/ItemService.java
package com.demo.erp.service;

import com.demo.erp.web.dto.ItemDto;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class ItemService {

    private final Map<String, ItemDto> store = new ConcurrentHashMap<>();

    public List<ItemDto> list(OffsetDateTime updatedSince) {
        return store.values().stream()
                .filter(i -> updatedSince == null || i.updatedAt().isAfter(updatedSince))
                .sorted(Comparator.comparing(ItemDto::updatedAt))
                .collect(Collectors.toList());
    }

    public void upsert(ItemDto dto) {
        store.put(dto.itemId(), dto);
    }

    // seed example
    public void seedIfEmpty() {
        if (store.isEmpty()) {
            var now = OffsetDateTime.now();
            upsert(new ItemDto("I-0001", "EA", now.minusHours(2), false));
            upsert(new ItemDto("I-0002", "EA", now.minusHours(1), false));
        }
    }
}