// performance/PerformanceQueryController.java
package com.globalmed.mes.mes_api.performance;

import com.globalmed.mes.mes_api.common.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

import static com.globalmed.mes.mes_api.performance.PerformanceSpecs.*;

@RestController
@RequestMapping("/performances")
@RequiredArgsConstructor
public class PerformanceQueryController {

    private final PerformanceRepo performanceRepo;

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

    // (선택) 검색 전용은 경로 분리
//    @GetMapping("/search")
//    public PageResponse<ProductionPerformanceEntity> search( /* 별도 파라미터 */ ) {  }
}