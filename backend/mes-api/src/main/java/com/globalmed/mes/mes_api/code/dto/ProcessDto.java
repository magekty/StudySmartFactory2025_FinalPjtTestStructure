package com.globalmed.mes.mes_api.code.dto;

import com.globalmed.mes.mes_api.process.domain.ProcessEntity;

// 공통 공정 정보 DTO
public record ProcessDto(
        String processId,
        String processName,
        String processDescription
) {
    public static ProcessDto fromEntity(ProcessEntity entity) {
        return new ProcessDto(
                entity.getProcessId(),
                entity.getProcessName(),
                entity.getDescription()
        );
    }
}