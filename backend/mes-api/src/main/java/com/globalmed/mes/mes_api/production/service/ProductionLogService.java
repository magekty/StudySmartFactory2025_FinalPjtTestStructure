package com.globalmed.mes.mes_api.production.service;

import com.globalmed.mes.mes_api.code.CodeEntity;
import com.globalmed.mes.mes_api.code.CodeRepo;
import com.globalmed.mes.mes_api.production.domain.ProductionLogEntity;
import com.globalmed.mes.mes_api.production.repository.ProductionLogRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductionLogService {

    private final ProductionLogRepo productionLogRepository;
    private final CodeRepo codeRepo;

    private CodeEntity getEventCode(String code) {
        return codeRepo.findByGroupCodeAndCodeAndUseYn("PROD_EVENT", code, 'Y')
                .orElseThrow(() -> new IllegalArgumentException("Invalid PROD_EVENT code: " + code));
    }

    /* 양품 생산 기록 */
    public void logGood(String workOrderId, String equipmentId, String processId, int goodQty) {
        try {
            ProductionLogEntity logGood = new ProductionLogEntity();
            logGood.setWorkOrderId(workOrderId);
            logGood.setEquipmentId(equipmentId);
            logGood.setProcessId(processId);
            logGood.setEventType(getEventCode("GOODQTY"));
            logGood.setEventValue(BigDecimal.valueOf(goodQty));
            logGood.setEventTimestamp(LocalDateTime.now());
            productionLogRepository.save(logGood);
        } catch (Exception ex) {
            throw new IllegalStateException("PROD_LOG_Good_ERROR: " + ex.getMessage(), ex);
        }
    }

    /* 불량 기록 */
    public void logDefect(String workOrderId, String equipmentId, String processId, int defectQty) {
        try {
            ProductionLogEntity logDefect = new ProductionLogEntity();
            logDefect.setWorkOrderId(workOrderId);
            logDefect.setEquipmentId(equipmentId);
            logDefect.setProcessId(processId);
            logDefect.setEventType(getEventCode("DEFECTQTY"));
            logDefect.setEventValue(BigDecimal.valueOf(defectQty));
            logDefect.setEventTimestamp(LocalDateTime.now());
            productionLogRepository.save(logDefect);
        } catch (Exception ex) {
            throw new IllegalStateException("PROD_LOG_Defect_ERROR: " + ex.getMessage(), ex);
        }
    }

    /* 시작 기록 */
    public void logStart(String workOrderId, String equipmentId, String processId) {
        try {
            ProductionLogEntity logStart = new ProductionLogEntity();
            logStart.setWorkOrderId(workOrderId);
            logStart.setEquipmentId(equipmentId);
            logStart.setProcessId(processId);
            logStart.setEventType(getEventCode("START"));
            logStart.setEventValue(BigDecimal.ZERO);
            logStart.setEventTimestamp(LocalDateTime.now());
            productionLogRepository.save(logStart);
        } catch (Exception ex) {
            throw new IllegalStateException("PROD_LOG_Start_ERROR: " + ex.getMessage(), ex);
        }
    }

    /* 종료 기록 */
    public void logEnd(String workOrderId, String equipmentId, String processId) {
        try {
            ProductionLogEntity logEnd = new ProductionLogEntity();
            logEnd.setWorkOrderId(workOrderId);
            logEnd.setEquipmentId(equipmentId);
            logEnd.setProcessId(processId);
            logEnd.setEventType(getEventCode("END"));
            logEnd.setEventValue(BigDecimal.ZERO);
            logEnd.setEventTimestamp(LocalDateTime.now());
            productionLogRepository.save(logEnd);
        } catch (Exception ex) {
            throw new IllegalStateException("PROD_LOG_END_ERROR: " + ex.getMessage(), ex);
        }
    }
    /* 장비 비가동 시작 기록 */
    @Transactional
    public void logDowntimeStart(String workOrderId, String equipmentId, String processId) {
        try {
            ProductionLogEntity startLog = new ProductionLogEntity();
            startLog.setWorkOrderId(workOrderId);
            startLog.setEquipmentId(equipmentId);
            startLog.setProcessId(processId);
            startLog.setEventType(getEventCode("DOWNTIME_START"));
            startLog.setEventValue(BigDecimal.ZERO);
            startLog.setEventTimestamp(LocalDateTime.now());
            productionLogRepository.save(startLog);
        } catch (Exception ex) {
            throw new IllegalStateException("PROD_LOG_DowntimeStart_ERROR: " + ex.getMessage(), ex);
        }
    }

    /* 장비 비가동 종료 기록 eventvalue = 초단위 */
    @Transactional
    public void logDowntimeEnd(String equipmentId) {
        try {
            Optional<ProductionLogEntity> lastStartOpt =
                    productionLogRepository.findTopByEquipmentIdAndEventType_CodeOrderByLogIdDesc(equipmentId, "DOWNTIME_START");

            if (lastStartOpt.isEmpty()) {
                throw new IllegalStateException("비가동 시작 로그가 없습니다.");
            }

            ProductionLogEntity startLog = lastStartOpt.get();
            LocalDateTime downtimeEndTime = LocalDateTime.now();
            long downtimeSeconds =
                    Duration.between(startLog.getEventTimestamp(), downtimeEndTime).toSeconds();

            ProductionLogEntity endLog = new ProductionLogEntity();
            endLog.setWorkOrderId(startLog.getWorkOrderId());
            endLog.setEquipmentId(equipmentId);
            endLog.setProcessId(startLog.getProcessId());
            endLog.setEventType(getEventCode("DOWNTIME_END"));
            endLog.setEventValue(BigDecimal.valueOf(downtimeSeconds));
            endLog.setEventTimestamp(downtimeEndTime);
            productionLogRepository.save(endLog);
        } catch (Exception ex) {
            throw new IllegalStateException("PROD_LOG_DowntimeEnd_ERROR: " + ex.getMessage(), ex);
        }
    }

}
