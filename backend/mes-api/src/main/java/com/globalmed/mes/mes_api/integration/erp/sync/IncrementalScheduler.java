// src/main/java/com/globalmed/mes/mes_api/integration/erp/sync/IncrementalScheduler.java
package com.globalmed.mes.mes_api.integration.erp.sync;

import com.globalmed.mes.mes_api.cursor.service.PlanSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class IncrementalScheduler {

    private final IncrementalSyncService incrementalSyncService; // items, boms
    private final PlanSyncService planSyncService;               // plans

    // 5분마다 증분
    @Scheduled(
            fixedDelayString = "${sync.fixedDelay:300000}",
            initialDelayString = "${sync.initialDelay:10000}"
    )
    public void run() {
        long startedNs = System.nanoTime();

        boolean itemsOk = false;
        boolean bomsOk = false;
        boolean plansOk = false;

        // 1) ITEMS
        try {
            incrementalSyncService.syncItems();
            itemsOk = true;
        } catch (Exception e) {
            log.error("[INCR] items sync failed - skip boms/plans", e);
        }

        // 2) BOMS (items 성공 시에만 진행)
        if (itemsOk) {
            try {
                incrementalSyncService.syncBoms();
                bomsOk = true;
            } catch (Exception e) {
                log.error("[INCR] boms sync failed - skip plans", e);
            }
        }

        // 3) PLANS (items/boms 모두 성공 시에만 진행)
        if (itemsOk && bomsOk) {
            try {
                planSyncService.sync();
                plansOk = true;
            } catch (Exception e) {
                log.error("[INCR] plans sync failed", e);
            }
        }

        long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedNs);
        log.info("[INCR] cycle done items={} boms={} plans={} tookMs={}",
                itemsOk ? "OK" : "FAIL",
                bomsOk ? "OK" : "FAIL",
                plansOk ? "OK" : "FAIL",
                tookMs);
    }
}