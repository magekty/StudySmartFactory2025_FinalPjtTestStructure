package com.globalmed.mes.mes_api.process.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

// 공정 생성 요청을 위한 DTO
public record ProcessCreationDto(

        @NotBlank(message = "공정명은 필수 항목입니다.")
        @Size(max = 255, message = "공정명은 최대 255자를 초과할 수 없습니다.")
        String processName,

        @Size(max = 255, message = "공정 설명은 최대 255자를 초과할 수 없습니다.")
        String description,
        @Size(max = 50, message = "자격 코드는 각각 최대 50자를 초과할 수 없습니다.")
        List<String> requiredCertCodes
) {}
