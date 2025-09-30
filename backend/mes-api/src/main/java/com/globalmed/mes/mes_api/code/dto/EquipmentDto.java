package com.globalmed.mes.mes_api.code.dto;

import com.globalmed.mes.mes_api.equipstatus.domain.EquipmentEntity;

// 공통 설비 정보 DTO
public record EquipmentDto(
        String equipmentId,
        String equipmentName,
        String statusCode,
        String workcenterId
) {
    public static EquipmentDto fromEntity(EquipmentEntity entity) {
        return new EquipmentDto(
                entity.getEquipmentId(),
                entity.getEquipmentName(),
                entity.getStatusCode().getCode(),
                entity.getWorkcenter().getWorkcenterId()
        );
    }
}