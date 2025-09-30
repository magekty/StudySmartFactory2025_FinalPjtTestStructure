package com.globalmed.mes.mes_api.cmms.service;

import com.globalmed.mes.mes_api.cmms.domain.*;
import com.globalmed.mes.mes_api.cmms.dto.*;
import com.globalmed.mes.mes_api.cmms.mapper.CmmsMapper;
import com.globalmed.mes.mes_api.cmms.repository.*;
import com.globalmed.mes.mes_api.code.CodeService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.time.OffsetDateTime;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class CmmsWorkOrderService {
    private final CmmsWorkOrderRepo repo;
    private final CmmsWorkOrderLogRepo logRepo;
    private final CodeService codes;

    private static final String G_WO_STATUS = "CMMS_WO_STATUS";
    private static final String S_OPEN      = "OPEN";
    private static final String S_ASSIGNED  = "ASSIGNED";
    private static final String S_INPROG    = "IN_PROGRESS";
    private static final String S_DONE      = "DONE";

    @Transactional
    public CmmsWorkOrderDto.Res create(CmmsWorkOrderDto.createReq req, String actorUserId){
        String requestId = req.getRequestId();
        if (requestId != null){
            Optional<CmmsWorkOrder> ex = repo.findByRequestId(requestId);
            if (ex.isPresent()) return CmmsMapper.toRes(ex.get());
        }

        Long openStatusId = codes.idOf(G_WO_STATUS, S_OPEN);

        CmmsWorkOrder e = CmmsWorkOrder.builder()
        .equipmentId(req.getEquipmentId())
        .title(req.getTitle())
        .priorityCodeId(req.getPriorityCodeId())
        .statusCodeId(openStatusId)
        .requestId(requestId)
        .build();

        if (actorUserId != null) {
            e.setCreatedBy(actorUserId);
        }

        CmmsWorkOrder saved = repo.save(e);

        log(saved.getId(), null, saved.getStatusCodeId(), "created", actorUserId);
        return CmmsMapper.toRes(saved);
    }

    @Transactional
    public CmmsWorkOrderDto.Res assign(Long id, CmmsWorkOrderDto.AssignReq req, String actorUserId){
        CmmsWorkOrder e = repo.findByIdAndDeletedFalse(id).orElseThrow();
        Long idOpen = codes.idOf(G_WO_STATUS, S_OPEN);
        Long idAssigned = codes.idOf(G_WO_STATUS, S_ASSIGNED);

        ensureCurrentIs(e.getStatusCodeId(), idOpen, "OPEN->ASSIGNED만 허용");
        Long from = e.getStatusCodeId();

        e.setStatusCodeId(idAssigned);
        e.setAssigneeUserId(req.getAssigneeUserId());
        CmmsWorkOrder saved = repo.save(e);
        log(saved.getId(), from, idAssigned, "assign", actorUserId);
        return CmmsMapper.toRes(saved);
    }

    @Transactional
    public CmmsWorkOrderDto.Res start(Long id, CmmsWorkOrderDto.startReq req, String actorUserId){
        CmmsWorkOrder e = repo.findByIdAndDeletedFalse(id).orElseThrow();
        Long idAssigned = codes.idOf(G_WO_STATUS, S_ASSIGNED);
        Long idInProg   = codes.idOf(G_WO_STATUS, S_INPROG);

        ensureCurrentIs(e.getStatusCodeId(), idAssigned, "ASSIGNED->IN_PROGRESS만 허용");
        Long from = e.getStatusCodeId();

        OffsetDateTime startedAt = (req.getStartedAt() != null) ? req.getStartedAt() : OffsetDateTime.now();
        e.setStatusCodeId(idInProg);
        e.setStartedAt(startedAt);
        CmmsWorkOrder saved = repo.save(e);

        log(saved.getId(), from, idInProg, "start", actorUserId);
        return CmmsMapper.toRes(saved);
    }

    @Transactional
    public CmmsWorkOrderDto.Res complete(Long id, CmmsWorkOrderDto.finishReq req, String actorUserId){
        CmmsWorkOrder e = repo.findByIdAndDeletedFalse(id).orElseThrow();
        Long idInProg = codes.idOf(G_WO_STATUS, S_INPROG);
        Long idDone   = codes.idOf(G_WO_STATUS, S_DONE);

        ensureCurrentIs(e.getStatusCodeId(), idInProg, "IN_PROGRESS->DONE만 허용");
        Long from = e.getStatusCodeId();

        OffsetDateTime finishedAt = req.getFinishedAt() != null ? req.getFinishedAt() : OffsetDateTime.now();
        e.setStatusCodeId(idDone);
        e.setFinishedAt(finishedAt);
        if (req.getActualMinutes() != null) e.setActualMinutes(req.getActualMinutes());
        if (req.getPartsCost()     != null) e.setPartsCost(req.getPartsCost());
        CmmsWorkOrder saved = repo.save(e);

        log(saved.getId(), from, idDone, "complete", actorUserId);
        return CmmsMapper.toRes(saved);
    }

    public Page<CmmsWorkOrderDto.Res> search(String status, String equipmentId, Pageable pageable){
        Long statusId = (status == null || status.isBlank()) ? null : codes.idOf(G_WO_STATUS, status);
        return repo.search(statusId, equipmentId, pageable).map(CmmsMapper::toRes);
    }

    private void ensureCurrentIs(Long currentStatusId, Long requiredCurrentId, String msgIfInvalid){
        if (currentStatusId == null || !currentStatusId.equals(requiredCurrentId)) {
            throw new IllegalStateException(msgIfInvalid);
        }
    }

    private void log(Long woId, Long fromId, Long toId, String note, String actorUserId){
        CmmsWorkOrderLog l = CmmsWorkOrderLog.builder()
        .cmmsWoId(woId)
        .fromStatusCodeId(fromId)
        .toStatusCodeId(toId)
        .changedByUserId(actorUserId)
        .note(note)
        .build();
        logRepo.save(l);
    }
}


