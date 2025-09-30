package com.globalmed.mes.mes_api.cmms.service;

import com.globalmed.mes.mes_api.cmms.domain.CmmsPmPlan;
import com.globalmed.mes.mes_api.cmms.dto.PmPlanDto;
import com.globalmed.mes.mes_api.cmms.mapper.CmmsMapper;
import com.globalmed.mes.mes_api.cmms.repository.CmmsPmPlanRepo;
import com.globalmed.mes.mes_api.code.CodeService;
import com.globalmed.mes.mes_api.kpi.downtime.service.PMPlanDowntimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
public class PmPlanService {

    private final CmmsPmPlanRepo repo;
    private final CodeService codes;
    private final PMPlanDowntimeService pmPlanDowntimeService;
    private static final String G_CYCLE = "CYCLE_TYPE";

    @Transactional
    public PmPlanDto.Res create(PmPlanDto.CreateReq req, String actorUserId){

        OffsetDateTime nextDueAt;
        if (req.getNextDueAt() != null) {
            nextDueAt = req.getNextDueAt();
        } else {
            OffsetDateTime base = (req.getLastDoneAt() != null) ? req.getLastDoneAt() : OffsetDateTime.now();
            String cycleCode = codes.codeOf(G_CYCLE, req.getCycleTypeCodeId());
            nextDueAt = computeNext(base, cycleCode, req.getCycleValue());
        }

        LocalDateTime localLDAt = req.getLastDoneAt().toLocalDateTime();
        LocalDateTime localNDAt = req.getNextDueAt().toLocalDateTime();
        OffsetDateTime utcLDAt = localLDAt
                .atZone(ZoneId.systemDefault()) // 서버 시간대 기준
                .toOffsetDateTime()             // OffsetDateTime으로 변환
                .withOffsetSameInstant(ZoneOffset.UTC); // UTC 기준;
        OffsetDateTime utcNDAt = localNDAt
                .atZone(ZoneId.systemDefault()) // 서버 시간대 기준
                .toOffsetDateTime()             // OffsetDateTime으로 변환
                .withOffsetSameInstant(ZoneOffset.UTC); // UTC 기준;;

        CmmsPmPlan e = CmmsPmPlan.builder()
        .equipmentId(req.getEquipmentId())
        .taskName(req.getTaskName())
        .cycleTypeCodeId(req.getCycleTypeCodeId())
        .cycleValue(req.getCycleValue())
        .lastDoneAt(utcLDAt)
        .nextDueAt(utcNDAt)
        .status("ACTIVE")
        .build();


        pmPlanDowntimeService.createPlannedDowntimeFromPmPlan(
                e.getEquipmentId(),
                nextDueAt,
                e.getEstimatedTakeTime(),
                e.getTaskName()
        );
        if(actorUserId != null){
            e.setCreatedBy(actorUserId);
        }

        CmmsPmPlan saved = repo.save(e);
        return CmmsMapper.toRes(saved);
    }

    @Transactional
    public PmPlanDto.Res markDoneAndRoll(Long planId, OffsetDateTime doneAt, String actorUserId){
        CmmsPmPlan plan = repo.findByIdAndDeletedFalse(planId).orElseThrow();

        LocalDateTime localDAt = doneAt.toLocalDateTime();
        OffsetDateTime utcDAt = localDAt
                .atZone(ZoneId.systemDefault()) // 서버 시간대 기준
                .toOffsetDateTime()             // OffsetDateTime으로 변환
                .withOffsetSameInstant(ZoneOffset.UTC); // UTC 기준;;
        plan.setLastDoneAt(utcDAt);


        String cycleCode = codes.codeOf(G_CYCLE, plan.getCycleTypeCodeId());
        plan.setNextDueAt(computeNext(utcDAt, cycleCode, plan.getCycleValue()));
        if(actorUserId != null){
            plan.setModifiedBy(actorUserId);
       }

        OffsetDateTime downTNDAt = computeNext(doneAt, cycleCode, plan.getCycleValue());
        pmPlanDowntimeService.createPlannedDowntimeFromPmPlan(
                plan.getEquipmentId(),
                downTNDAt,
                plan.getEstimatedTakeTime(),
                plan.getTaskName()
        );
        
        CmmsPmPlan saved = repo.save(plan);
        return CmmsMapper.toRes(saved);
    }

    public Page<PmPlanDto.Res> findDue(OffsetDateTime to, String equipmentId, Pageable pageable){
        return repo.findDue(to, equipmentId, pageable).map(CmmsMapper::toRes);
    }

    private OffsetDateTime computeNext(OffsetDateTime base, String cycleCode, int cycleValue) {
        switch (cycleCode) {
            case "HOURS":    return base.plusHours(cycleValue);
            case "DAYS":     return base.plusDays(cycleValue);
            case "CALENDAR": return base.plusDays(cycleValue); // 월주기 원하면 plusMonths로 교체
            default: throw new IllegalArgumentException("Unknown cycle code: " + cycleCode);
        }
    }
}