// src/main/java/com/demo/erp/service/PlanService.java
package com.demo.erp.service;

import com.demo.erp.web.dto.PlanUpsertDto;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PlanService {

    private record Key(String planId, Integer lineNo) {}
    private record PlanRow(String planId, Integer lineNo, String itemId, double qty, OffsetDateTime dueDateUtc, Integer priority, boolean isDeleted, OffsetDateTime updatedAt) {}

    private final Map<Key, PlanRow> plans = new ConcurrentHashMap<>();

    public boolean upsert(PlanUpsertDto dto) {
        Key key = new Key(dto.planId(), dto.planLineNo());
        boolean created = !plans.containsKey(key);
        plans.put(key, new PlanRow(
                dto.planId(), dto.planLineNo(), dto.itemId(),
                dto.qty() == null ? 0 : dto.qty(),
                dto.dueDateUtc(), dto.priority(), dto.isDeleted(),
                OffsetDateTime.now()
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
}