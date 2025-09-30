package com.globalmed.mes.mes_api.code.dto;

import com.globalmed.mes.mes_api.code.CodeEntity;

public record CodeDto (
        Long codeId,
        String code,
        String codeName
) {
    public static CodeDto fromEntity(CodeEntity e) {
        if (e == null) return null;
        return new CodeDto(e.getCodeId(), e.getCode(), e.getName());
    }

}
