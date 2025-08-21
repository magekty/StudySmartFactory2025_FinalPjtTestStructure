package com.globalmed.mes.mes_api.equipstatus;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/equip-status")
@RequiredArgsConstructor
public class EquipmentStatusController {

    private final EquipmentStatusService svc;

    @PreAuthorize("@permChecker.has(authentication, '/equip-status','write') or hasAnyRole('ADMIN','OP')")
    @PostMapping
    public ResponseEntity<?> create(@RequestBody EquipmentStatusService.EquipStatusReq req) {
        var saved = svc.startRun(req);
        return ResponseEntity.status(201).body(Map.of(
                "logId", saved.getLogId(),
                "equipmentId", saved.getEquipmentId(),
                "status", saved.getStatusCode().getCode(),
                "startTime", saved.getStartTime()
        ));
    }
}