// src/main/java/com/globalmed/mes/mes_api/performance/service/PerformanceAppService.java
package com.globalmed.mes.mes_api.performance.service;

import com.globalmed.mes.mes_api.log.ProdLogService;
import com.globalmed.mes.mes_api.performance.domain.ProductionPerformanceEntity;
import com.globalmed.mes.mes_api.performance.repository.PerformanceRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
public class PerformanceAppService {

    private final ProdLogService prodLogService;
    private final PerformanceRepo performanceRepository;

    @Transactional
    public String record(String woId, String itemId, String processId, String eqpId,
                         double goodQty, double defectQty, String uom,
                         OffsetDateTime startUtc, OffsetDateTime endUtc, String requestId) {

        ProductionPerformanceEntity p = new ProductionPerformanceEntity();
        p.setWorkOrderId(woId);
        p.setItemId(itemId);
        p.setProcessId(processId);
        p.setEquipmentId(eqpId);
        p.setProducedQty(BigDecimal.valueOf(goodQty + defectQty));
        p.setDefectQty(BigDecimal.valueOf(defectQty));
        p.setStartTime(startUtc.withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime());
        p.setEndTime(endUtc.withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime());
        if (requestId != null && !requestId.isBlank()) {
            p.setRequestId(requestId);
        }

        p = performanceRepository.save(p);
        Long perfId = p.getPerformanceId();

        String goodKey = (requestId != null && !requestId.isBlank())
                ? "REQ:" + requestId + ":GOOD"
                : "PERF:" + perfId + ":GOOD";
        String defectKey = (requestId != null && !requestId.isBlank())
                ? "REQ:" + requestId + ":DEFECT"
                : "PERF:" + perfId + ":DEFECT";

        if (goodQty > 0) {
            prodLogService.goodQty(woId, itemId, processId, eqpId, goodQty, uom,
                    endUtc.withOffsetSameInstant(ZoneOffset.UTC), goodKey);
        }
        if (defectQty > 0) {
            prodLogService.defectQty(woId, itemId, processId, eqpId, defectQty, uom,
                    endUtc.withOffsetSameInstant(ZoneOffset.UTC), defectKey);
        }

        return String.valueOf(perfId);
    }
}