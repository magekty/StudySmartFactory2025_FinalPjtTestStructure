package com.globalmed.mes.mes_api.equipstatus.controller;

import com.globalmed.mes.mes_api.equipstatus.dto.EquipDetailDto;
import com.globalmed.mes.mes_api.equipstatus.service.EquipmentDetailService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/equip-detail")
@RequiredArgsConstructor
public class EquipmentDetailController {

    private final EquipmentDetailService equipmentDetailService;

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable("id") String equipmentId, HttpServletRequest req) {
        try {
            EquipDetailDto detail = equipmentDetailService.getEquipmentDetail(equipmentId);

            if (detail == null) {
                return ResponseEntity.status(404).body(Map.of(
                        "code", "ID_NOT_FOUND",
                        "message", "해당 장비 ID를 찾을 수 없습니다",
                        "path", req.getRequestURI(),
                        "method", req.getMethod()
                ));
            }
            return ResponseEntity.ok(detail);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "code", "INTERNAL_ERROR",
                    "message", e.getMessage(),
                    "path", req.getRequestURI(),
                    "method", req.getMethod()
            ));
        }
    }
}
