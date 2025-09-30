package com.globalmed.mes.mes_api.cmms.mapper;

import com.globalmed.mes.mes_api.cmms.dto.*;
import com.globalmed.mes.mes_api.cmms.domain.*;

public final class CmmsMapper {
    private CmmsMapper(){}

    public static PmPlanDto.Res toRes(CmmsPmPlan e){
        return new PmPlanDto.Res(e.getId(), e.getEquipmentId(), e.getTaskName(),
                e.getCycleTypeCodeId(), e.getCycleValue(), e.getLastDoneAt(), e.getNextDueAt(), e.getStatus());
    }

    public static CmmsWorkOrderDto.Res toRes(CmmsWorkOrder e){
        return new CmmsWorkOrderDto.Res(e.getId(), e.getEquipmentId(), e.getTitle(),
                e.getStatusCodeId(), e.getPriorityCodeId(), e.getAssigneeUserId(), e.getRequestId(),
                e.getCreatedAt(), e.getStartedAt(), e.getFinishedAt(), e.getActualMinutes(), e.getPartsCost()
        );
    }

    public static FaultLogDto.Res toRes(CmmsFaultLog e){
        return new FaultLogDto.Res(e.getId(),e.getEquipmentId(), e.getLossCategoryCodeId(), e.getSymptom()
        , e.getAction(), e.getOccurredAt(), e.getResolvedAt(), e.getCmmsWoId());
    }

}
