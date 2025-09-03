// src/main/java/com/globalmed/mes/mes_api/config/ShadowProps.java
package com.globalmed.mes.mes_api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "erp.shadow")
public record ShadowProps(
        boolean enabled,
        boolean workOrders,
        boolean performances,
        boolean backflush,
        boolean cost
) {}