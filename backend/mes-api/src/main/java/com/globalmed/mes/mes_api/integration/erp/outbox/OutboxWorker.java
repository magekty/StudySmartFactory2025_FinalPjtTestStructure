package com.globalmed.mes.mes_api.integration.erp.outbox;

import com.globalmed.mes.mes_api.integration.erp.ErpClient;
import com.globalmed.mes.mes_api.integration.erp.ErpProps;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.*;

@Component
public class OutboxWorker {

    private final OutboxRepository repo;
    private final ErpClient erp;
    private final ErpProps props;
    private final int batchSize;
    private final int maxRetry;
    private final long[] backoffSeq;

    public OutboxWorker(OutboxRepository repo, ErpClient erp, ErpProps props,
                        @Value("${outbox.worker.batch-size:100}") int batchSize,
                        @Value("${outbox.worker.max-retry:5}") int maxRetry,
                        @Value("${outbox.worker.backoff-seq:60000,300000,900000,3600000,21600000}") String seq) {
        this.repo = repo; this.erp = erp; this.props = props;
        this.batchSize = batchSize; this.maxRetry = maxRetry;
        this.backoffSeq = Arrays.stream(seq.split(",")).mapToLong(Long::parseLong).toArray();
    }

    @Scheduled(fixedDelayString = "${outbox.worker.delay-ms:1000}")
    public void run() {
        var list = repo.pickBatch(List.of(OutboxStatus.PENDING, OutboxStatus.RETRY), OffsetDateTime.now(), PageRequest.of(0, batchSize));
        for (OutboxEntity o : list) {
            try {
                // idempotencyKey 없으면 생성/저장
                if (o.getIdempotencyKey() == null || o.getIdempotencyKey().isBlank()) {
                    o.setIdempotencyKey(UUID.randomUUID().toString());
                }

                // Shadow 분기
                if (isShadow(o.getEventType())) {
                    o.setStatus(OutboxStatus.SHADOWED);
                    o.setNextRetryAt(null);
                    o.setLastError(null);
                    repo.save(o);
                    continue;
                }

                ResponseEntity<String> res = erp.send(o.getEventType(),
                        o.getPayloadJson(),
                        o.getIdempotencyKey());
                int sc = res.getStatusCode().value();
                if (sc >= 200 && sc < 300) {
                    o.setStatus(OutboxStatus.SENT);
                    o.setNextRetryAt(null);
                    o.setLastError(null);
                } else {
                    scheduleRetry(o, "HTTP_" + sc);
                }
            } catch (Exception ex) {
                scheduleRetry(o, ex.getClass().getSimpleName() + ":" + ex.getMessage());
            }
            repo.save(o);
        }
    }

    private boolean isShadow(String type) {
        if (!props.shadow().enabled()) return false;
        return switch (type) {
            case "BACKFLUSH"   -> props.shadow().backflush();
            case "COST_POST"   -> props.shadow().cost();
            case "WO_CREATE"   -> props.shadow().workOrders();
            case "WO_STATUS"   -> props.shadow().workOrders();
            case "PERF_CREATE" -> props.shadow().performances();
            default -> true; // 알 수 없으면 안전하게 Shadow
        };
    }

    private void scheduleRetry(OutboxEntity o, String error) {
        int rc = o.getRetryCount() + 1;
        o.setRetryCount(rc);
        o.setStatus(rc >= maxRetry ? OutboxStatus.FAILED : OutboxStatus.RETRY);
        long wait = backoffSeq[Math.min(rc - 1, backoffSeq.length - 1)];
        o.setNextRetryAt(rc >= maxRetry ? null : OffsetDateTime.now().plusSeconds(wait / 1000));
        o.setLastError(error);
    }
}