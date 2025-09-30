package com.globalmed.mes.mes_api.cmms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.OffsetDateTime;


public class FaultLogDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "FaultLogCreateReq")
    public static class CreateReq{
            @NotBlank String equipmentId;
            @NotNull Long lossCategoryCodeId;
            @NotBlank String symptom;
            String action;
            @NotNull OffsetDateTime occurredAt;
            OffsetDateTime resolvedAt;
            Long cmmsWoId;
}
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "FaultLogRes")
    public static class Res{
            Long id;
            String equipmentId;
            Long lossCategoryCodeId;
            String symptom;
            String action;
            OffsetDateTime occurredAt;
            OffsetDateTime resolvedAt;
            Long workOrderId;
    }
}
