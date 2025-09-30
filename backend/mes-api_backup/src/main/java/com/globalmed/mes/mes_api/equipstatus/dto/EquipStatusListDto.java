package com.globalmed.mes.mes_api.equipstatus.dto;

import java.time.LocalDateTime;

public record EquipStatusListDto(
        Long logId,
        String equipmentId,
        String status,          // CodeEntity.code (RUN/IDLE/DOWN)
        LocalDateTime startTime,
        LocalDateTime endTime
) {}