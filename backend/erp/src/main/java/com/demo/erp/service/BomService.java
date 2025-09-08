// src/main/java/com/demo/erp/service/BomService.java
package com.demo.erp.service;

import com.demo.erp.web.dto.BomHeaderDto;
import com.demo.erp.web.dto.BomLineDto;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class BomService {

    private final Map<String, BomHeaderDto> headers = new ConcurrentHashMap<>();

    public List<BomHeaderDto> list(OffsetDateTime updatedSince) {
        return headers.values().stream()
                .filter(h -> updatedSince == null || h.updatedAt().isAfter(updatedSince))
                .sorted(Comparator.comparing(BomHeaderDto::updatedAt))
                .collect(Collectors.toList());
    }

    public void upsert(BomHeaderDto dto) {
        headers.put(dto.bomId(), dto);
    }

    // seed example
    public void seedIfEmpty() {
        if (headers.isEmpty()) {
            var now = OffsetDateTime.now();
//            upsert(new BomHeaderDto(
//                    "BOM-TEST-01", "I-0001", "A",
//                    now, null,
//                    now, false,
//                    List.of(new BomLineDto(1,"RM-001", 1.0, "EA", 0.0, now, false),
//                            new BomLineDto(2,"RM-002", 0.5, "EA", 0.0, now, false),
//                            new BomLineDto(3,"RM-003", 2.0, "EA", 0.0, now, false))
//            ));
        }
    }
}