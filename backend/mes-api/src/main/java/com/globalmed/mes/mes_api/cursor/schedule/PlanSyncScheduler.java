// src/main/java/com/globalmed/mes/mes_api/cursor/schedule/PlanSyncScheduler.java
package com.globalmed.mes.mes_api.cursor.schedule;

import com.globalmed.mes.mes_api.cursor.service.PlanSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlanSyncScheduler {

    private final PlanSyncService planSyncService;

    // 기본 60초 주기, 10초 뒤 시작. 프로퍼티로 조절 가능.
    @Scheduled(
            fixedDelayString = "${sync.plan.fixedDelay:60000}",
            initialDelayString = "${sync.plan.initialDelay:10000}"
    )
    public void tick() {
        try {
            planSyncService.sync();
        } catch (Exception e) {
            log.error("plan sync failed", e);
        }
    }
}