package com.globalmed.mes.mes_api.process.dto;

import com.globalmed.mes.mes_api.code.dto.CertDto;
import com.globalmed.mes.mes_api.code.dto.EquipmentDto;

import java.time.OffsetDateTime;
import java.util.List;

// 공정 상세정보 조회용 DTO
public record ProcessDetailDto(
        // 공정 UUID
        String id,
        // 공정 이름
        String name,
        // 공정 설명
        String description,
        // 이 공정을 하는 설비 목록
        List<EquipmentDto> equipments,
        // 이 공정에 필요한 자격증
        List<CertDto> requiredCerts,
        // 마지막 수정일
        OffsetDateTime LastmodAt,
        // 마지막 수정자
        String LastmodBy
) {}