package com.globalmed.mes.mes_api.equipstatus;

import com.globalmed.mes.mes_api.code.CodeEntity;
import com.globalmed.mes.mes_api.code.CodeRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.*;

@Service
@RequiredArgsConstructor
public class EquipmentStatusService {
    private final EquipmentStatusRepo repo;
    private final CodeRepo codeRepo;

    @Transactional
    public EquipmentStatusLogEntity startRun(EquipStatusReq req) {
        // 상태코드(eq p_status) 조회
        CodeEntity status = codeRepo.findByGroupCodeAndCodeAndUseYn("eqp_status", req.statusCode(), 'Y')
                .orElseThrow(() -> new IllegalStateException("EQP_STATUS_NOT_FOUND"));

        LocalDateTime start = parseUtc(req.startTimeUtc());
        LocalDateTime end = req.endTimeUtc() == null || req.endTimeUtc().isBlank() ? null : parseUtc(req.endTimeUtc());
        if (end != null && end.isBefore(start)) throw new IllegalArgumentException("TIME_ORDER_INVALID");

        EquipmentStatusLogEntity log = new EquipmentStatusLogEntity();
        log.setEquipmentId(req.equipmentId());
        log.setStatusCode(status);
        log.setStartTime(start);
        log.setEndTime(end);
        // 필요 시 workOrderId/shiftId/reasonCode 추가 세팅
        return repo.save(log);
    }

    private LocalDateTime parseUtc(String isoZ) {
        // "2025-08-10T09:00:00Z" → UTC LocalDateTime
        return OffsetDateTime.parse(isoZ).atZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
    }

    public record EquipStatusReq(String equipmentId, String statusCode, String startTimeUtc, String endTimeUtc) {}
}