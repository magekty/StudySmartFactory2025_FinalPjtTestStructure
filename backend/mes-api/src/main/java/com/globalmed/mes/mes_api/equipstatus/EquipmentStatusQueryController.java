package com.globalmed.mes.mes_api.equipstatus;

import com.globalmed.mes.mes_api.common.PageResponse;
import com.globalmed.mes.mes_api.equipstatus.dto.EquipStatusListDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/equip-status")
@RequiredArgsConstructor
public class EquipmentStatusQueryController {

    private final EquipmentStatusRepo repo;

    @GetMapping
    public ResponseEntity<PageResponse<EquipStatusListDto>> list(
            @RequestParam String equipmentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "startTime,desc") String sort) {

        String[] sp = sort.split(",");
        Sort s = (sp.length == 2 && "asc".equalsIgnoreCase(sp[1]))
                ? Sort.by(sp[0]).ascending()
                : Sort.by(sp[0]).descending();
        Pageable pageable = PageRequest.of(page, size, s);

        Page<EquipmentStatusLogEntity> result;
        if (from != null && to != null) {
            result = repo.findByEquipmentIdAndStartTimeBetweenOrderByStartTimeDesc(equipmentId, from, to, pageable);
        } else {
            result = repo.findByEquipmentIdOrderByStartTimeDesc(equipmentId, pageable);
        }

        var dtoPage = result.map(log -> new EquipStatusListDto(
                log.getLogId(),
                log.getEquipmentId(),
                (log.getStatusCode() != null ? log.getStatusCode().getCode() : null),
                log.getStartTime(),
                log.getEndTime()
        ));
        return ResponseEntity.ok(PageResponse.of(dtoPage, sort));
    }
}