// src/main/java/com/globalmed/mes/mes_api/plan/api/PlanIssueController.java
package com.globalmed.mes.mes_api.plan.api;

import com.globalmed.mes.mes_api.plan.service.PlanIssueService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/internal/plans")
public class PlanIssueController {

    private final PlanIssueService svc;
    public PlanIssueController(PlanIssueService svc){ this.svc = svc; }

    public static record IssueReq(
            BigDecimal issueQty,
            String issueUom,
            String processId,
            String equipmentId,
            boolean force,
            String remark
    ) {}

    public static record IssueRes(
            String workOrderId,
            BigDecimal issuedQty,
            String issuedAtUtc
    ) {}

    @PostMapping("/{planId}/{lineNo}/issue-wo")
    public ResponseEntity<IssueRes> issue(@PathVariable @NotBlank String planId,
                                          @PathVariable @NotNull Integer lineNo,
                                          @RequestBody IssueReq req) {
        var res = svc.issueWo(planId, lineNo, req);
        return ResponseEntity.status(201).body(res);
    }
}