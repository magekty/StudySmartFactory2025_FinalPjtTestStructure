// src/main/java/com/globalmed/mes/mes_api/config/SyncProperties.java
package com.globalmed.mes.mes_api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sync")
public class SyncProperties {

    private Integer fixedDelay = 300000;   // ms
    private Integer initialDelay = 10000;  // ms
    private Audit audit = new Audit();
    private Boms boms = new Boms();

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

    public Integer getFixedDelay() { return fixedDelay; }
    public void setFixedDelay(Integer fixedDelay) { this.fixedDelay = fixedDelay; }
    public Integer getInitialDelay() { return initialDelay; }
    public void setInitialDelay(Integer initialDelay) { this.initialDelay = initialDelay; }
    public Audit getAudit() { return audit; }
    public void setAudit(Audit audit) { this.audit = audit; }
    public Boms getBoms() { return boms; }
    public void setBoms(Boms boms) { this.boms = boms; }
}