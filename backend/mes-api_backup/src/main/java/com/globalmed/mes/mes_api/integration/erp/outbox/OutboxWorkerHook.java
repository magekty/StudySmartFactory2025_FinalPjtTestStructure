package com.globalmed.mes.mes_api.integration.erp.outbox;

// src/main/java/com/globalmed/mes/mes_api/integration/outbox/OutboxWorkerHook.java
// 예시: 백플러시 처리 시 듀얼 라이트 훅

import com.globalmed.mes.mes_api.log.ProdLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Component
@RequiredArgsConstructor
public class OutboxWorkerHook {

    private final ProdLogService prodLogService;

    public void onBackflushLogged(String woId, String itemId, double totalQty, String uom, String outcome, String outboxKey) {
        prodLogService.backflush(
                woId, itemId, totalQty, uom, outcome,
                OffsetDateTime.now(ZoneOffset.UTC),
                "BF:" + outboxKey, null
        );
    }
}