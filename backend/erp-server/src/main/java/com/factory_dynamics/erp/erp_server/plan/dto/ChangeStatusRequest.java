package com.factory_dynamics.erp.erp_server.plan.dto;

public record ChangeStatusRequest(
        String nextStatus,
        String actor,
        long version
) {}