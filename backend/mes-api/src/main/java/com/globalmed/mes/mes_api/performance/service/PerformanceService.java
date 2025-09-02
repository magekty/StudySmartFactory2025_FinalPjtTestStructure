// src/main/java/com/globalmed/mes/mes_api/performance/service/PerformanceService.java
package com.globalmed.mes.mes_api.performance.service;

import com.globalmed.mes.mes_api.log.ProdLogService;
import com.globalmed.mes.mes_api.performance.domain.ProductionPerformanceEntity;
import com.globalmed.mes.mes_api.performance.repository.PerformanceRepo;
import com.globalmed.mes.mes_api.workorder.domain.WorkOrderEntity;
import com.globalmed.mes.mes_api.workorder.repository.WorkOrderRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.*;

@Service
@RequiredArgsConstructor
public class PerformanceService {

    private final PerformanceRepo performanceRepo;
    private final WorkOrderRepo workOrderRepo;
    private final ProdLogService prodLogService;

    public record Req(
            String workOrderId, String itemId, String processId, String equipmentId,
            BigDecimal producedQty, BigDecimal defectQty,
            String startTime, String endTime, String requestId
    ) {}

    public record Res(Long performanceId, BigDecimal goodQty) {}

    @Transactional
    public Res create(Req req) {
        String woId = t(req.workOrderId());
        String item = t(req.itemId());
        String proc = t(req.processId());
        String eqp  = t(req.equipmentId());
        String rid  = t(req.requestId());

        if (req.producedQty() == null || req.defectQty() == null
                || req.workOrderId() == null || req.itemId() == null
                || req.processId() == null || req.equipmentId() == null
                || req.startTime() == null || req.endTime() == null) {
            throw new IllegalArgumentException("VALIDATION_ERROR");
        }
        if (req.producedQty().compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("VALIDATION_ERROR");
        if (req.defectQty().compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("VALIDATION_ERROR");
        if (req.defectQty().compareTo(req.producedQty()) > 0) throw new IllegalArgumentException("VALIDATION_ERROR");

        LocalDateTime st = toUtcLdt(req.startTime());
        LocalDateTime et = toUtcLdt(req.endTime());
        if (et.isBefore(st)) throw new IllegalArgumentException("TIME_ORDER_INVALID");

        WorkOrderEntity wo = workOrderRepo.findById(woId)
                .orElseThrow(() -> new IllegalArgumentException("NOT_FOUND"));
        String cur = (wo.getStatusCode() != null ? wo.getStatusCode() : null);
        if (!"R".equals(cur)) throw new IllegalStateException("WO_STATUS_INVALID");

        LocalDateTime baseline = (wo.getStartTs() != null) ? wo.getStartTs() : wo.getCreatedAt();
        if (baseline != null) {
            if (st.isBefore(baseline) || et.isBefore(baseline)) {
                throw new IllegalArgumentException("PERF_BEFORE_WO");
            }
        }
        if (rid != null && !rid.isEmpty() && performanceRepo.findByRequestId(rid).isPresent()) {
            throw new IllegalStateException("DUPLICATE_KEY");
        }

        var p = new ProductionPerformanceEntity();
        p.setWorkOrderId(woId);
        p.setItemId(item);
        p.setProcessId(proc);
        p.setEquipmentId(eqp);
        p.setProducedQty(req.producedQty());
        p.setDefectQty(req.defectQty());
        p.setStartTime(st);
        p.setEndTime(et);
        if (rid != null && !rid.isEmpty()) p.setRequestId(rid);

        try {
            p = performanceRepo.save(p);
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            throw new IllegalStateException("DUPLICATE_KEY");
        }

        if (wo.getProducedQty() == null) {
            wo.setProducedQty(BigDecimal.ZERO);
        }
        wo.setProducedQty(wo.getProducedQty().add(req.producedQty()));

        BigDecimal good = req.producedQty().subtract(req.defectQty());

        // 듀얼 라이트(B안): 저장 직후 로그 적재
        OffsetDateTime endUtc = et.atOffset(ZoneOffset.UTC);
        String goodKey = (rid != null && !rid.isEmpty())
                ? "REQ:" + rid + ":GOOD"
                : "PERF:" + p.getPerformanceId() + ":GOOD";
        String defectKey = (rid != null && !rid.isEmpty())
                ? "REQ:" + rid + ":DEFECT"
                : "PERF:" + p.getPerformanceId() + ":DEFECT";

        if (good.compareTo(BigDecimal.ZERO) > 0) {
            prodLogService.goodQty(woId, item, proc, eqp, good.doubleValue(), "EA", endUtc, goodKey);
        }
        if (req.defectQty().compareTo(BigDecimal.ZERO) > 0) {
            prodLogService.defectQty(woId, item, proc, eqp, req.defectQty().doubleValue(), "EA", endUtc, defectKey);
        }

        return new Res(p.getPerformanceId(), good);
    }

    private static String t(String s){ return s==null ? null : s.trim(); }
    private LocalDateTime toUtcLdt(String isoZ) {
        return OffsetDateTime.parse(isoZ).atZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
    }
}