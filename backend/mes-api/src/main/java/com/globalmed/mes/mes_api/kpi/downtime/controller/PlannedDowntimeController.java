package com.globalmed.mes.mes_api.kpi.downtime.controller;

import com.globalmed.mes.mes_api.kpi.downtime.domain.PlannedDowntimeEntity;
import com.globalmed.mes.mes_api.kpi.downtime.dto.PlannedDowntimeDto;
import com.globalmed.mes.mes_api.kpi.downtime.service.PlannedDowntimeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/planned-downtime")
@RequiredArgsConstructor
public class PlannedDowntimeController {

    private final PlannedDowntimeService plannedDowntimeService;

    /**
     * 새로운 계획된 다운타임 기록을 생성
     */
    @PreAuthorize("@permChecker.has(authentication, '/work-orders','write') or hasAnyRole('ADMIN','OP')")
    @PostMapping
    public ResponseEntity<PlannedDowntimeEntity> addPlannedDowntime(@Valid @RequestBody PlannedDowntimeDto dto) {
        PlannedDowntimeEntity savedEntity = plannedDowntimeService.addPlannedDowntime(dto);
        return new ResponseEntity<>(savedEntity, HttpStatus.CREATED);
    }
}
