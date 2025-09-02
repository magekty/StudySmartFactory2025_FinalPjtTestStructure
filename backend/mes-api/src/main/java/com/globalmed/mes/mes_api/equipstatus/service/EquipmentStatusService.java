// src/main/java/com/globalmed/mes/mes_api/equipstatus/service/EquipmentStatusService.java
package com.globalmed.mes.mes_api.equipstatus.service;

import com.globalmed.mes.mes_api.code.CodeEntity;
import com.globalmed.mes.mes_api.code.CodeRepo;
import com.globalmed.mes.mes_api.equipstatus.domain.EquipmentStatusLogEntity;
import com.globalmed.mes.mes_api.equipstatus.repository.EquipmentStatusRepo;
import com.globalmed.mes.mes_api.log.ProdLogService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
public class EquipmentStatusService {

    private static final String GROUP_EQP_STATUS = "EQP_STATUS";

    private final EquipmentStatusRepo repo;
    private final CodeRepo codeRepo;
    private final ProdLogService prodLogService;

    @Transactional
    public EquipmentStatusLogEntity startRun(EquipStatusReq req) {
        String eqpId = req.equipmentId();
        String statusCode = normalize(req.statusCode());

        CodeEntity status = codeRepo.findByGroupCodeAndCodeAndUseYn(GROUP_EQP_STATUS, statusCode, 'Y')
                .orElseThrow(() -> new IllegalStateException("EQP_STATUS_NOT_FOUND"));

        LocalDateTime start = parseUtc(req.startTimeUtc());
        LocalDateTime end = (req.endTimeUtc() == null || req.endTimeUtc().isBlank()) ? null : parseUtc(req.endTimeUtc());
        if (end != null && end.isBefore(start)) throw new IllegalArgumentException("TIME_ORDER_INVALID");

        EquipmentStatusLogEntity log = new EquipmentStatusLogEntity();
        log.setEquipmentId(eqpId);
        log.setStatusCode(status);
        log.setStartTime(start);
        log.setEndTime(end);

        EquipmentStatusLogEntity saved = repo.save(log);

        // 듀얼 라이트(설비 상태) — 시작 시각 기준
        prodLogService.equipmentStatus(
                eqpId,
                statusCode,
                start.atOffset(ZoneOffset.UTC),
                "EQP-STS:" + eqpId + ":" + statusCode + ":" + saved.getLogId()
        );

        return saved;
    }

    @Transactional
    public EquipmentStatusLogEntity changeStatus(String eqpId, String status, OffsetDateTime atUtc) {
        String statusCode = normalize(status);

        CodeEntity code = codeRepo.findByGroupCodeAndCodeAndUseYn(GROUP_EQP_STATUS, statusCode, 'Y')
                .orElseThrow(() -> new IllegalStateException("EQP_STATUS_NOT_FOUND"));

        OffsetDateTime ts = (atUtc != null) ? atUtc.withOffsetSameInstant(ZoneOffset.UTC) : OffsetDateTime.now(ZoneOffset.UTC);
        LocalDateTime start = ts.toLocalDateTime();

        EquipmentStatusLogEntity log = new EquipmentStatusLogEntity();
        log.setEquipmentId(eqpId);
        log.setStatusCode(code);
        log.setStartTime(start);
        log.setEndTime(null);

        EquipmentStatusLogEntity saved = repo.save(log);

        // 듀얼 라이트(설비 상태)
        prodLogService.equipmentStatus(
                eqpId,
                statusCode,
                ts,
                "EQP-STS:" + eqpId + ":" + statusCode + ":" + saved.getLogId()
        );

        return saved;
    }

    private LocalDateTime parseUtc(String isoZ) {
        return OffsetDateTime.parse(isoZ).withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime();
    }

    private String normalize(String code) {
        return code == null ? null : code.trim().toUpperCase();
    }

    public record EquipStatusReq(String equipmentId, String statusCode, String startTimeUtc, String endTimeUtc) {}
}