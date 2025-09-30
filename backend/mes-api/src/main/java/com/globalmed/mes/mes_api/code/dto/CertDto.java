package com.globalmed.mes.mes_api.code.dto;

import com.globalmed.mes.mes_api.employee.cert.domain.CertEntity;

// 공통 자격증 정보 DTO
public record CertDto(
        String certCode,
        String certName,
        String certDescription
) {
    public static CertDto fromEntity(CertEntity entity) {
        return new CertDto(
                entity.getCertCode(),
                entity.getCertName(),
                entity.getCertDescription()
        );
    }
}