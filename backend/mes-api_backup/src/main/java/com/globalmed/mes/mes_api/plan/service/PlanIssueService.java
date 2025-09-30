// src/main/java/com/globalmed/mes/mes_api/plan/service/PlanIssueService.java
package com.globalmed.mes.mes_api.plan.service;

import com.globalmed.mes.mes_api.plan.api.PlanIssueController.IssueReq;
import com.globalmed.mes.mes_api.plan.api.PlanIssueController.IssueRes;
import com.globalmed.mes.mes_api.plan.domain.PlanWoMap;
import com.globalmed.mes.mes_api.plan.repository.PlanWoMapRepository;
import com.globalmed.mes.mes_api.plan.repository.ProductionPlanLineRepository;
import com.globalmed.mes.mes_api.workorder.service.WorkOrderService;
import com.globalmed.mes.mes_api.workorder.domain.WorkOrderEntity;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlanIssueService {

    private final ProductionPlanLineRepository lineRepo;
    private final PlanWoMapRepository mapRepo;
    private final WorkOrderService workOrderService;

    @Transactional
    public IssueRes issueWo(String planId, Integer lineNo, IssueReq req) {
        var line = lineRepo.findByPlanIdAndPlanLineNo(planId, lineNo)
                .orElseThrow(() -> new IllegalArgumentException("PLAN_LINE_NOT_FOUND"));
        if (Boolean.TRUE.equals(line.getIsDeleted())) throw new IllegalStateException("PLAN_LINE_DELETED");

        var planQty = line.getQty();
        var issued = mapRepo.sumIssuedQty(planId, lineNo);
        var remaining = planQty.subtract(issued);
        if (remaining.signum() < 0) remaining = BigDecimal.ZERO;

        var reqQty = req.issueQty() != null ? req.issueQty() : remaining;
        if (reqQty == null || reqQty.signum() <= 0) throw new IllegalArgumentException("ISSUE_QTY_INVALID");
        if (!req.force() && reqQty.compareTo(remaining) > 0) throw new IllegalStateException("ISSUE_QTY_EXCEEDS_REMAIN");

        String processId = req.processId();
        String eqpId = req.equipmentId();
        if (processId == null || processId.isBlank() || eqpId == null || eqpId.isBlank())
            throw new IllegalArgumentException("PROCESS_OR_EQUIP_REQUIRED");

        String woNumber = "WO-" + planId + "-" + lineNo + "-" + System.currentTimeMillis();
        WorkOrderEntity wo = workOrderService.create(
                woNumber, line.getItemId(), processId, eqpId, reqQty, "issue"
        );

        var map = new PlanWoMap();
        map.setPlanId(planId);
        map.setPlanLineNo(lineNo);
        map.setWorkOrderId(wo.getWorkOrderId());
        map.setIssueQty(reqQty);
        map.setIssueUom(req.issueUom() == null || req.issueUom().isBlank() ? "EA" : req.issueUom());
        map.setIssuedBy("issue");
        map.setIssuedAt(OffsetDateTime.now(ZoneOffset.UTC));
        map.setRemark(req.remark());
        map.setStatus("ISSUED");
        mapRepo.save(map);

        return new IssueRes(wo.getWorkOrderId(), reqQty, map.getIssuedAt().toString());
    }
}