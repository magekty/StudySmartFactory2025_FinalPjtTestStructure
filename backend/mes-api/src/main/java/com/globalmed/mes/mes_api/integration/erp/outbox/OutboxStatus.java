package com.globalmed.mes.mes_api.integration.erp.outbox;

public enum OutboxStatus {
    PENDING, SENT, RETRY, FAILED, SHADOWED
}