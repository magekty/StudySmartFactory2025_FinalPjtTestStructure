package com.globalmed.mes.mes_api.equipstatus.service;

import com.globalmed.mes.mes_api.code.CodeEntity;
import com.globalmed.mes.mes_api.equipstatus.domain.EquipmentStatusLogEntity;
import com.globalmed.mes.mes_api.production.service.ProductionLogService;
import com.globalmed.mes.mes_api.workorder.domain.WorkOrderEntity;
import com.globalmed.mes.mes_api.workorder.repository.WorkOrderRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EquipmentDowntimeLogService {
    private final ProductionLogService productionLogService;
    private final WorkOrderRepo workOrderRepo;
    public void handleStatusChange(EquipmentStatusLogEntity lastLog, CodeEntity newStatus, EquipmentStatusService.EquipStatusReq req) {
        if (lastLog == null) return;

        String prevStatus = lastLog.getStatusCode().getCode();
        String currStatus = newStatus.getCode();

        // 해당 설비의 가장 최근의 워크오더가 R상태라면 가져오고 아니라면 가져오지 않음
        Optional<WorkOrderEntity> optWo = workOrderRepo.findFirstByEquipmentId_EquipmentIdOrderByCreatedAtDesc(req.equipmentId());
        String workOrderId = optWo.filter(wo -> "R".equals(wo.getStatusCode().getCode()))
                .map(WorkOrderEntity::getWorkOrderId)
                .orElse(null);
        String processId = optWo.filter(wo -> "R".equals(wo.getStatusCode().getCode()))
                .map(wo -> wo.getProcessId().getProcessId())
                .orElse(null);


        // RUN → IDLE/DOWN (다운타임 시작)
        if ("RUN".equals(prevStatus) && ("IDLE".equals(currStatus) || "DOWN".equals(currStatus))) {
            productionLogService.logDowntimeStart(workOrderId, req.equipmentId(), processId);
        }

        // IDLE/DOWN → RUN (다운타임 종료)
        if (("IDLE".equals(prevStatus) || "DOWN".equals(prevStatus)) && "RUN".equals(currStatus)) {
            productionLogService.logDowntimeEnd(req.equipmentId());
        }
    }
}
