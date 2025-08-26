package com.globalmed.mes.mes_api.performance.controller;

import com.globalmed.mes.mes_api.common.PageResponse;
import com.globalmed.mes.mes_api.performance.repository.PerformanceRepo;
import com.globalmed.mes.mes_api.performance.service.PerformanceService;
import com.globalmed.mes.mes_api.performance.specs.PerformanceSpecs;
import com.globalmed.mes.mes_api.performance.domain.ProductionPerformanceEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/performances")
@RequiredArgsConstructor
public class PerformanceController {

    private final PerformanceService performanceService;
    private final PerformanceRepo performanceRepo;

    @PreAuthorize("@permChecker.has(authentication, '/performances','write') or hasAnyRole('ADMIN','OP')")
    @PostMapping
    public ResponseEntity<?> create(@RequestBody PerformanceService.Req req) {
        var res = performanceService.create(req);
        return ResponseEntity.status(201).body(Map.of(
                "performanceId", res.performanceId(),
                "goodQty", res.goodQty()
        ));
    }
    @GetMapping(params = {"page","size"}) // ← 충돌 방지
    public PageResponse<ProductionPerformanceEntity> list(
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam(defaultValue = "startTime,desc") String sort,
            @RequestParam(required = false) String equipmentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        String[] sp = sort.split(",");
        Sort s = (sp.length == 2 && "asc".equalsIgnoreCase(sp[1]))
                ? Sort.by(sp[0]).ascending() : Sort.by(sp[0]).descending();
        Pageable pageable = PageRequest.of(page, size, s);

        Specification<ProductionPerformanceEntity> spec = Specification.allOf(
                PerformanceSpecs.equipmentIdEquals(equipmentId),
                PerformanceSpecs.startBetween(from, to)
        );

        Page<ProductionPerformanceEntity> result = performanceRepo.findAll(spec, pageable);
        return PageResponse.of(result, sort);
    }

    // (선택) 상세 조회는 경로로 분리
    @GetMapping("/{id}")
    public ResponseEntity<ProductionPerformanceEntity> get(@PathVariable Long id) {
        return ResponseEntity.of(performanceRepo.findById(id));
    }
}