package com.demo.erp.web.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import java.time.OffsetDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PlanUpsertDto(
        @NotBlank String planId,
        @NotNull Integer planLineNo,
        @NotBlank String itemId,
        @PositiveOrZero Double qty,
        @NotNull OffsetDateTime dueDateUtc,
        Integer priority,
        @JsonProperty("isDeleted") @JsonAlias({"deleted","is_deleted"}) Boolean isDeleted
) {
    public PlanUpsertDto {
        if (qty == null) qty = 0d;
        // null로 들어오면 false로 디폴트
        isDeleted = (isDeleted != null && isDeleted);
    }
}