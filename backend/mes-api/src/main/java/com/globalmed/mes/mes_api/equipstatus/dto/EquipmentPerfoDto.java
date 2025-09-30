package com.globalmed.mes.mes_api.equipstatus.dto;

import com.globalmed.mes.mes_api.equipstatus.domain.EquipmentEntity;

import java.time.LocalDateTime;

public record EquipmentPerfoDto(
        String equipmentId,
        String equipmentName,
        String statusCodeId,
        boolean isDeleted,
        LocalDateTime deletedAt,
        String createdBy,
        LocalDateTime createdAt,
        String modifiedBy,
        LocalDateTime modifiedAt
) {
    public static EquipmentPerfoDto fromEntity(EquipmentEntity e) {
        if (e == null) return null;
        return new EquipmentPerfoDto(
                e.getEquipmentId(),
                e.getEquipmentName(),
//                WorkcenterPerfoDto.fromEntity(e.getWorkcenter()),
//                ProcessPerfoDto.fromEntity(e.getProcess()),
                e.getStatusCode() != null ? e.getStatusCode().getCode() : null,
                e.isDeleted(),
                e.getDeletedAt(),
                e.getCreatedBy(),
                e.getCreatedAt(),
                e.getModifiedBy(),
                e.getModifiedAt()
        );
    }
}
