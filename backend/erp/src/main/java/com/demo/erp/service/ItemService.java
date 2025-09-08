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
                .filter(i -> updatedSince == null || (i.updatedAt()!=null && i.updatedAt().isAfter(updatedSince)))
                .sorted(Comparator.comparing(ItemDto::updatedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
    }

    public void upsert(ItemDto dto) {
        store.put(dto.itemId(), dto);
    }

    public void seedIfEmpty() {
        if (store.isEmpty()) {
            var now = OffsetDateTime.now();
//            upsert(new ItemDto("I-0001","I-0001","BOX","F", now.minusHours(2), false));
//            upsert(new ItemDto("I-0002", "I-0002","EA","F", now.minusHours(2), false));
//            upsert(new ItemDto("RM-001","RM-001","EA","R", now.minusHours(1), true));
//            upsert(new ItemDto("RM-002","RM-002","EA","R", now.minusMinutes(30), false));
//            upsert(new ItemDto("RM-003","RM-003","EA","R", now.plusHours(2), false));
//            upsert(new ItemDto("RM-004","RM-004","EA","R", now.plusHours(4), false));
        }
    }
}