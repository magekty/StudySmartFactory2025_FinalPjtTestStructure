package com.globalmed.mes.mes_api.equipstatus;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/equip-status")
@RequiredArgsConstructor
public class EquipmentStatusController {

    private final EquipmentStatusService svc;

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