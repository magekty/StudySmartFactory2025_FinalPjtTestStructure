package com.factory_dynamics.erp.erp_server.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProductCreateRequest(
        @NotBlank @Size(max = 50) String productCode,
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(max = 10) String type,
        @NotBlank @Size(max = 20) String unit,
        @Size(max = 255) String description
) {}