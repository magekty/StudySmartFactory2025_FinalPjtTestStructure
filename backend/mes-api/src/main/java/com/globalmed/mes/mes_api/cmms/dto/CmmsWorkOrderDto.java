package com.globalmed.mes.mes_api.cmms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class CmmsWorkOrderDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "WorkOrderCreateReq")
    public static class createReq{
            @NotBlank private String equipmentId;
            @NotBlank private String title;
            @NotNull private Long priorityCodeId;
            private String requestId;
}

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssignReq{@NotBlank private String assigneeUserId;}

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class finishReq{ private OffsetDateTime finishedAt; private Integer actualMinutes; private BigDecimal partsCost;}

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class startReq{private OffsetDateTime startedAt;}

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    @Builder
    @Schema(name = "WorkOrderRes")
    public static class Res {
        Long id;
        String equipmentId;
        String title;
        Long statusCodeId;
        Long priorityCodeId;
        String assigneeUserId;
        String requestId;
        OffsetDateTime createdAt;
        OffsetDateTime startedAt;
        OffsetDateTime finishedAt;
        Integer actualMinutes;
        BigDecimal partsCost;
    }
}
