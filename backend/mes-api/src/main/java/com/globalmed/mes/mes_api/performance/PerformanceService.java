package com.globalmed.mes.mes_api.performance;

import com.globalmed.mes.mes_api.workorder.WorkOrderEntity;
import com.globalmed.mes.mes_api.workorder.WorkOrderRepo;
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

    public record Req(
            String workOrderId, String itemId, String processId, String equipmentId,
            BigDecimal producedQty, BigDecimal defectQty,
            String startTime, String endTime, String requestId // ISO8601, 예: 2025-08-10T09:00:00Z
    ) {}

    public record Res(Long performanceId, BigDecimal goodQty) {}

    @Transactional
    public Res create(Req req) {
        String woId = t(req.workOrderId());
        String item = t(req.itemId());
        String proc = t(req.processId());
        String eqp  = t(req.equipmentId());
        String rid  = t(req.requestId());

        // 필수값
        if (req.producedQty() == null || req.defectQty() == null
                || req.workOrderId() == null || req.itemId() == null
                || req.processId() == null || req.equipmentId() == null
                || req.startTime() == null || req.endTime() == null) {
            throw new IllegalArgumentException("VALIDATION_ERROR");
        }

        // 수량 검증
        if (req.producedQty().compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("VALIDATION_ERROR");
        if (req.defectQty().compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("VALIDATION_ERROR");
        if (req.defectQty().compareTo(req.producedQty()) > 0) throw new IllegalArgumentException("VALIDATION_ERROR");

        // 시간 파싱(UTC) 및 검증
        LocalDateTime st = toUtcLdt(req.startTime());
        LocalDateTime et = toUtcLdt(req.endTime());
        if (et.isBefore(st)) throw new IllegalArgumentException("TIME_ORDER_INVALID");

        // WO 상태 검증(Released만 허용)
        WorkOrderEntity wo = workOrderRepo.findById(req.workOrderId())
                .orElseThrow(() -> new IllegalArgumentException("NOT_FOUND"));
        String cur = (wo.getStatusCode() != null ? wo.getStatusCode().getCode() : null);
        if (!"R".equals(cur)) throw new IllegalStateException("WO_STATUS_INVALID");

        // WorkOrderEntity 조회 직후, 상태 R 검증 바로 다음에 배치
        LocalDateTime baseline = (wo.getStartTs() != null) ? wo.getStartTs() : wo.getCreatedAt();
        // st/et는 이미 OffsetDateTime→UTC LocalDateTime 변환된 값
        if (baseline != null) {
            if (st.isBefore(baseline) || et.isBefore(baseline)) {
                throw new IllegalArgumentException("PERF_BEFORE_WO"); // 400으로 매핑됨
            }
        }
        if (rid != null && !rid.isEmpty() && performanceRepo.findByRequestId(rid).isPresent()) {
            throw new IllegalStateException("DUPLICATE_KEY");
        }
        // 저장
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

        // 누적 갱신
        wo.setProducedQty(wo.getProducedQty().add(req.producedQty()));

        BigDecimal good = req.producedQty().subtract(req.defectQty());
        return new Res(p.getPerformanceId(), good);
    }

    private static String t(String s){ return s==null ? null : s.trim(); }
    private LocalDateTime toUtcLdt(String isoZ) {
        return OffsetDateTime.parse(isoZ).atZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
    }
}