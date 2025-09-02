package com.globalmed.mes.mes_api.integration.erp.sync;

import com.globalmed.mes.mes_api.cursor.service.PlanSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IncrementalScheduler {
    private final IncrementalSyncService incrementalSyncService; // items, boms
    private final PlanSyncService planSyncService;              // plans

    // 5분마다 증분
    @Scheduled(fixedDelayString = "${sync.fixedDelay:300000}",
            initialDelayString = "${sync.initialDelay:10000}")
    public void run() {
        incrementalSyncService.syncItems();
        incrementalSyncService.syncBoms();
        planSyncService.sync();
    }
}