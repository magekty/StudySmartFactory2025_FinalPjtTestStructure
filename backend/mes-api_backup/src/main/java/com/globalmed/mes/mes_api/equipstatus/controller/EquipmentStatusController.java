package com.globalmed.mes.mes_api.equipstatus.controller;

import com.globalmed.mes.mes_api.common.PageResponse;
import com.globalmed.mes.mes_api.equipstatus.repository.EquipmentStatusRepo;
import com.globalmed.mes.mes_api.equipstatus.service.EquipmentStatusService;
import com.globalmed.mes.mes_api.equipstatus.domain.EquipmentStatusLogEntity;
import com.globalmed.mes.mes_api.equipstatus.dto.EquipStatusListDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/equip-status")
@RequiredArgsConstructor
public class EquipmentStatusController {

    private final EquipmentStatusService equipmentStatusService;
    private final EquipmentStatusRepo equipmentStatusRepo;

    @PreAuthorize("@permChecker.has(authentication, '/equip-status','write') or hasAnyRole('ADMIN','OP')")
    @PostMapping
    public ResponseEntity<?> create(@RequestBody EquipmentStatusService.EquipStatusReq req) {
        var saved = equipmentStatusService.startRun(req);
        return ResponseEntity.status(201).body(Map.of(
                "logId", saved.getLogId(),
                "equipmentId", saved.getEquipmentId(),
                "status", saved.getStatusCode().getCode(),
                "startTime", saved.getStartTime()
        ));
    }
    @GetMapping
    public ResponseEntity<PageResponse<EquipStatusListDto>> list(
            @RequestParam String equipmentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "startTime,desc") String sort) {

        String[] sp = sort.split(",");
        Sort s = (sp.length == 2 && "asc".equalsIgnoreCase(sp[1]))
                ? Sort.by(sp[0]).ascending()
                : Sort.by(sp[0]).descending();
        Pageable pageable = PageRequest.of(page, size, s);

        Page<EquipmentStatusLogEntity> result;
        if (from != null && to != null) {
            result = equipmentStatusRepo.findByEquipmentIdAndStartTimeBetweenOrderByStartTimeDesc(equipmentId, from, to, pageable);
        } else {
            result = equipmentStatusRepo.findByEquipmentIdOrderByStartTimeDesc(equipmentId, pageable);
        }

        var dtoPage = result.map(log -> new EquipStatusListDto(
                log.getLogId(),
                log.getEquipmentId(),
                (log.getStatusCode() != null ? log.getStatusCode().getCode() : null),
                log.getStartTime(),
                log.getEndTime()
        ));
        return ResponseEntity.ok(PageResponse.of(dtoPage, sort));
    }
}