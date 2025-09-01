package com.globalmed.mes.mes_api.integration.erp;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "erp")
public record ErpProps(
        String baseUrl, String apiKey,
        Shadow shadow
) {
    public record Shadow(boolean enabled, boolean workOrders, boolean performances, boolean backflush, boolean cost){}
}