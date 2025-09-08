package com.demo.erp.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

// ERP(Mock)
@Configuration
@EnableConfigurationProperties(MockProps.class)
@ConfigurationProperties(prefix = "mock")
public class MockProps {

    private boolean enabled = true;
    private Boms boms = new Boms();

    @Getter
    @Setter
    public static class Boms {
        private int maxRows = 2;
        // getters/setters
    }

    @PostConstruct
    void logConfig() {
        var log = org.slf4j.LoggerFactory.getLogger(getClass());
        log.info("[MOCK-ERP] cfg enabled={}, boms.maxRows={}", enabled, boms.getMaxRows());
    }
}