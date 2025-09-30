package com.globalmed.mes.mes_api.equipstatus.service;

import com.globalmed.mes.mes_api.code.CodeEntity;
import com.globalmed.mes.mes_api.code.CodeRepo;
import com.globalmed.mes.mes_api.equipstatus.domain.EquipmentStatusLogEntity;
import com.globalmed.mes.mes_api.equipstatus.repository.EquipmentStatusRepo;
import com.globalmed.mes.mes_api.production.service.ProductionLogService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.*;

@Service
@RequiredArgsConstructor
public class EquipmentStatusService {
    private final EquipmentStatusRepo repo;
    private final CodeRepo codeRepo;
    private final EquipmentDowntimeLogService downtimeLogService;

    @Transactional
    public EquipmentStatusLogEntity startRun(EquipStatusReq req) {
        // 상태코드(eq p_status) 조회
        CodeEntity status = codeRepo.findByGroupCodeAndCodeAndUseYn("eqp_status", req.statusCode(), 'Y')
                .orElseThrow(() -> new IllegalStateException("EQP_STATUS_NOT_FOUND"));

        LocalDateTime start = parseUtc(req.startTimeUtc());
        LocalDateTime end = req.endTimeUtc() == null || req.endTimeUtc().isBlank() ? null : parseUtc(req.endTimeUtc());
        if (end != null && end.isBefore(start)) throw new IllegalArgumentException("TIME_ORDER_INVALID");

        // downtime용 직전 상태 조회
        EquipmentStatusLogEntity lastLog = repo.findTopByEquipmentIdOrderByStartTimeDesc(req.equipmentId())
                .orElse(null);

        if (lastLog != null) {
            String lastCode = lastLog.getStatusCode().getCode();
            String newCode = status.getCode();

            // 1) RUN -> RUN: 에러
            if ("RUN".equals(newCode) && newCode.equals(lastCode)) {
                throw new IllegalArgumentException("이미 가동되고 있습니다.");
            }

            // 2) IDLE -> IDLE 또는 DOWN -> DOWN: 기존 로그 갱신
            if (("IDLE".equals(newCode) && newCode.equals(lastCode)) ||
                    ("DOWN".equals(newCode) && newCode.equals(lastCode))) {

                lastLog.setEndTime(end); // 끝 시간 갱신
                return repo.save(lastLog); // 갱신된 로그 반환
            }
        }


        // 다운타임 로직은 분리된 서비스에서 처리
        downtimeLogService.handleStatusChange(lastLog, status, req);



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