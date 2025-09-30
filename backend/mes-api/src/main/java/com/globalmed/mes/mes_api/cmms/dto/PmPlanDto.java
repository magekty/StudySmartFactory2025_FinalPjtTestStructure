package com.globalmed.mes.mes_api.cmms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;


import java.time.OffsetDateTime;

public class PmPlanDto{

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "PmPlanCreateReq",
            description = "PM 계획 생성 요청")
    public static class CreateReq{
            @NotBlank String equipmentId;
            @NotBlank String taskName;
            @NotNull Long cycleTypeCodeId;
            @NotNull Integer cycleValue;
            OffsetDateTime lastDoneAt;
            OffsetDateTime nextDueAt;
            Integer estimatedTakeTime;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "PmPlanRes")
    public static class Res{
            Long id;
            String equipmentId;
            String taskName;
            Long cycleTypeCodeId;
            Integer cycleValue;
            OffsetDateTime lastDoneAt;
            OffsetDateTime nextDueAt;
            String status;
    }
}

