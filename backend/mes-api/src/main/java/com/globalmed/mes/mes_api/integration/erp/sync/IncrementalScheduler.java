package com.globalmed.mes.mes_api.integration.erp.sync;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IncrementalScheduler {

    private final IncrementalSyncService svc;

    // 5분마다 증분
    @Scheduled(fixedDelay = 30_000, initialDelay = 10_000)
    public void run() {
        svc.syncItems();
        svc.syncBoms();
        // Plans는 내부 스키마/키에 맞춰 추후 연결(필요 시 추가 메서드 작성)
    }
}