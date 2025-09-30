package com.globalmed.mes.mes_api.process.dto;

import com.globalmed.mes.mes_api.process.domain.ProcessEntity;
import java.time.LocalDateTime;

public record ProcessPerfoDto(
        String processId,
        String processName,
        String description,
        boolean isDeleted,
        LocalDateTime deletedAt,
        String createdBy,
        LocalDateTime createdAt,
        String modifiedBy,
        LocalDateTime modifiedAt
) {
    public static ProcessPerfoDto fromEntity(ProcessEntity e) {
        if (e == null) return null;
        return new ProcessPerfoDto(
                e.getProcessId(),
                e.getProcessName(),
                e.getDescription(),
                e.isDeleted(),
                e.getDeletedAt(),
                e.getCreatedBy(),
                e.getCreatedAt(),
                e.getModifiedBy(),
                e.getModifiedAt()
        );
    }
}
