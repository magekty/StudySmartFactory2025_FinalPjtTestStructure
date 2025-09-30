// src/main/java/com/globalmed/mes/mes_api/config/SyncProperties.java
package com.globalmed.mes.mes_api.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@ConfigurationProperties(prefix = "sync")
public class SyncProperties {

    private Integer fixedDelay = 300000;   // ms
    private Integer initialDelay = 10000;  // ms
    private Audit audit = new Audit();
    private Boms boms = new Boms();
    private Cursor cursor = new Cursor();
    private Scheduler scheduler = new Scheduler();

    public static class Audit {
        private boolean enabled = true;
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }

    public static class Boms {
        private Integer maxHops = 1000;
        private boolean skipUnknownComponent = false;
        public Integer getMaxHops() { return maxHops; }
        public void setMaxHops(Integer maxHops) { this.maxHops = maxHops; }
        public boolean isSkipUnknownComponent() { return skipUnknownComponent; }
        public void setSkipUnknownComponent(boolean skipUnknownComponent) { this.skipUnknownComponent = skipUnknownComponent; }
    }
    @Setter
    @Getter
    public static class Cursor {
        // ε를 나노초로 고정 관리(밀리/마이크로 환경이면 변환해서 사용)
        private long epsilonNanos = 1L;
        // getters/setters
    }
    @Setter
    @Getter
    public static class Scheduler {
        private int intervalSeconds = 30;
        // getters/setters
    }

    @PostConstruct
    void logConfig() {
        org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(getClass());
        log.info("[BOM_SYNC] cfg skipUnknown={}, epsilon={}ns, intervalSec={}",
                boms.isSkipUnknownComponent(), cursor.getEpsilonNanos(), scheduler.getIntervalSeconds());
    }

}