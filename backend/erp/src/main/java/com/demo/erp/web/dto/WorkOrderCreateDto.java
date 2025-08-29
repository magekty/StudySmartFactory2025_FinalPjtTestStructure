package com.demo.erp.web.dto;
import jakarta.validation.constraints.*;
import java.time.OffsetDateTime;

public record WorkOrderCreateDto(
        @NotBlank String workOrderId, @NotBlank String workOrderNumber, @NotBlank String itemId,
        @PositiveOrZero Double qty, @Pattern(regexp="P|R|C") String status, OffsetDateTime startTs) {}