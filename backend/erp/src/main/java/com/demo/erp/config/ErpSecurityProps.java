// src/main/java/com/demo/erp/config/ErpSecurityProps.java
package com.demo.erp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "erp.security")
public record ErpSecurityProps(
        boolean enabled
) {
    public ErpSecurityProps() { this(true); }
}