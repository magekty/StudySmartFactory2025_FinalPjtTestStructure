package com.globalmed.mes.mes_api.kpi.controller;


import com.globalmed.mes.mes_api.common.PageResponse;
import com.globalmed.mes.mes_api.kpi.domain.KpiDataEntity;
import com.globalmed.mes.mes_api.kpi.dto.KpiDataListDto;
import com.globalmed.mes.mes_api.kpi.repository.KpiDataRepo;
import com.globalmed.mes.mes_api.kpi.service.KpiService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/kpi")
@RequiredArgsConstructor
public class KpiController {
    private final KpiDataRepo kpiDataRepo;
    private final KpiService kpiService;

    // GET /kpi/actuals?kpiDate=2025-08-10&equipmentId=E-0001
    @GetMapping("/actuals")
    public ResponseEntity<?> actuals(@RequestParam String kpiDate,
                                     @RequestParam String equipmentId) {
        LocalDate date = LocalDate.parse(kpiDate); // ISO yyyy-MM-dd
        KpiService.Res res  = kpiService.getActuals(date, equipmentId);
        return ResponseEntity.ok(Map.of(
                "kpiDate", res.kpiDate(),
                "equipmentId", res.equipmentId(),
                "targetOee", res.targetOee(),
                "targetProductivity", res.targetProductivity(),
                "targetYield", res.targetYield(),
                "actualOutput", res.actualOutput(),
                "actualYield", res.actualYield()
        ));
    }

    /**
     * KPI 데이터를 날짜, 장비, 공정, 품목, 집계 유형별로 조회
     */
    @GetMapping("/datalist")
    public ResponseEntity<PageResponse<KpiDataListDto>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate kpiDate,
            @RequestParam(required = false) String equipmentId,
            @RequestParam(required = false) String processId,
            @RequestParam(required = false) String itemId,
            @RequestParam(required = false) Long aggregationTypeId,
            @RequestParam(defaultValue = "kpiDate,desc") String sort) {

        Sort s = Sort.by(sort.split(",")[0]).descending();
        if (sort.split(",").length > 1 && sort.split(",")[1].equalsIgnoreCase("asc")) {
            s = Sort.by(sort.split(",")[0]).ascending();
        }
        Pageable pageable = PageRequest.of(page, size, s);

        // 검색 조건에 따라 적절한 Repository 메서드 호출
        Page<KpiDataEntity> result;
        if (kpiDate != null) {
            result = kpiDataRepo.findByKpiDateAndFilters(kpiDate, equipmentId, processId, itemId, aggregationTypeId, pageable);
        } else {
            result = kpiDataRepo.findByFilters(equipmentId, processId, itemId, aggregationTypeId, pageable);
        }

        Page<KpiDataListDto> dtoPage = result.map(KpiDataListDto::fromEntity);

        return ResponseEntity.ok(PageResponse.of(dtoPage, sort));
    }

}