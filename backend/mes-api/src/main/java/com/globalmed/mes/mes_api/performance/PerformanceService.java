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

    private final PerformanceRepo perfRepo;
    private final WorkOrderRepo woRepo;

    @Transactional
    public Result create(Req req) {
        // 필수 검증
        if (req.producedQty() == null || req.producedQty().compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("VALIDATION_ERROR");
        if (req.defectQty() == null || req.defectQty().compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("VALIDATION_ERROR");
        if (req.defectQty().compareTo(req.producedQty()) > 0)
            throw new IllegalArgumentException("VALIDATION_ERROR");
        LocalDateTime st = parseUtc(req.startTime());
        LocalDateTime et = parseUtc(req.endTime());
        if (et.isBefore(st)) throw new IllegalArgumentException("TIME_ORDER_INVALID");

        // WO 상태 검증(R 권장) — 상태코드 확인 로직이 있으면 여기서 체크
        WorkOrderEntity wo = woRepo.findById(req.workOrderId())
                .orElseThrow(() -> new IllegalArgumentException("NOT_FOUND"));

        // PERF 저장
        var p = new ProductionPerformanceEntity();
        p.setWorkOrderId(req.workOrderId());
        p.setItemId(req.itemId());
        p.setProcessId(req.processId());
        p.setEquipmentId(req.equipmentId());
        p.setProducedQty(req.producedQty());
        p.setDefectQty(req.defectQty());
        p.setStartTime(st);
        p.setEndTime(et);
        var saved = perfRepo.save(p);

        // WO 누적 합산
        wo.setProducedQty(wo.getProducedQty().add(req.producedQty())); // dirty checking

        BigDecimal good = req.producedQty().subtract(req.defectQty());
        return new Result(saved.getPerformanceId(), good);
    }

    private LocalDateTime parseUtc(String isoZ) {
        return OffsetDateTime.parse(isoZ).atZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
    }

    public record Req(
            String workOrderId, String itemId, String processId, String equipmentId,
            BigDecimal producedQty, BigDecimal defectQty, String startTime, String endTime
    ){}
    public record Result(Long performanceId, BigDecimal goodQty){}
}