// ERP: src/main/java/com/demo/erp/service/PlanService.java
package com.demo.erp.service;

import com.demo.erp.web.dto.PlanLineView;
import com.demo.erp.web.dto.PlanUpsertDto;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PlanService {

    private record Key(String planId, Integer lineNo) {}
    private record Row(String planId, Integer lineNo, String itemId, double qty,
                       OffsetDateTime dueDateUtc, Integer priority, String unit,
                       boolean isDeleted, OffsetDateTime updatedAt) {}

    private final Map<Key, Row> store = new ConcurrentHashMap<>();

    public boolean upsert(PlanUpsertDto dto) {
        Key key = new Key(dto.planId(), dto.planLineNo());
        boolean created = !store.containsKey(key);
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        store.put(key, new Row(
                dto.planId(), dto.planLineNo(), dto.itemId(),
                dto.qty() == null ? 0 : dto.qty(),
                dto.dueDateUtc(),
                dto.priority(),
                (dto.unit()==null || dto.unit().isBlank()) ? "EA" : dto.unit(),
                dto.isDeleted(),
                now
        ));
        return created;
    }

    public ResponseEntity<Void> post(PlanUpsertDto dto) {
        boolean created = upsert(dto);
        return created ? ResponseEntity.status(201).build() : ResponseEntity.ok().build();
    }

    public ResponseEntity<Void> put(PlanUpsertDto dto) {
        boolean created = upsert(dto);
        return created ? ResponseEntity.status(201).build() : ResponseEntity.ok().build();
    }

    public List<PlanLineView> list(OffsetDateTime updatedSince, int page, int size) {
        return store.values().stream()
                .filter(r -> updatedSince == null || (r.updatedAt != null && r.updatedAt.isAfter(updatedSince)))
                .sorted(Comparator.comparing(Row::updatedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .skip((long) page * size)
                .limit(size)
                .map(r -> new PlanLineView(r.planId, r.lineNo, r.itemId, r.qty, r.dueDateUtc, r.priority, r.unit, r.isDeleted, r.updatedAt))
                .toList();
    }
}