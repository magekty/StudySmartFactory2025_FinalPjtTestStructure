package com.globalmed.mes.mes_api.kpi.downtime.dto;

import java.time.LocalDateTime;

public record DowntimeIntervalDto(LocalDateTime start, LocalDateTime end) {}
