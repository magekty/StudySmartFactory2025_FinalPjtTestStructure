package com.globalmed.mes.mes_api.integration.erp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class ErpClient {
    private final RestClient rc;
    private final ObjectMapper om;

    public ResponseEntity<String> send(String eventType, String payloadJson, String idempotencyKey) {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            h.add("X-Idempotency-Key", idempotencyKey);
        }
        HttpEntity<String> req = new HttpEntity<>(payloadJson, h);

        String path = switch (eventType) {
            case "WO_CREATE"   -> "/erp/work-orders";
            case "WO_STATUS"   -> "/erp/work-orders/" + extractWoId(payloadJson) + "/status";
            case "PERF_CREATE" -> "/erp/performances";
            case "BACKFLUSH"   -> "/erp/consumptions/backflush";
            default -> throw new IllegalArgumentException("UNKNOWN_EVENT");
        };

        if ("WO_STATUS".equals(eventType)) {
            return rc.put().uri(path).headers(http -> http.addAll(h)).body(payloadJson).retrieve().toEntity(String.class);
        } else {
            return rc.post().uri(path).headers(http -> http.addAll(h)).body(payloadJson).retrieve().toEntity(String.class);
        }
    }

    // 최소 파서(필요 시 JSON 파서로 교체)
    private String extractWoId(String json) {
        try {
            JsonNode n = om.readTree(json);
            JsonNode v = n.get("workOrderId");
            if (v != null && v.isTextual()) return v.asText();
        } catch (Exception ignore) {}
        throw new IllegalArgumentException("WORK_ORDER_ID_MISSING");
    }
}