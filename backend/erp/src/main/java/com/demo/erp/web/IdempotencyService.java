// src/main/java/com/demo/erp/web/IdempotencyService.java
package com.demo.erp.web;
import com.github.benmanes.caffeine.cache.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.util.Objects;

@Service
public class IdempotencyService {
    private final Cache<String, Cached> cache;
    public IdempotencyService(@Value("${idempotency.ttlHours:72}") int ttlHours) {
        this.cache = Caffeine.newBuilder().expireAfterWrite(Duration.ofHours(ttlHours)).maximumSize(100_000).build();
    }
    private String key(String method, String path, String idem) {
        return method + "|" + path + "|" + idem;
    }
    public ResponseEntity<?> replayIfPresent(String method, String path, String idem) {
        if (idem == null || idem.isBlank()) return null;
        Cached c = cache.getIfPresent(key(method, path, idem));
        if (c == null) return null;
        return ResponseEntity.status(c.status).headers(h -> h.addAll(c.headers)).body(c.body);
    }
    public void store(String method, String path, String idem, ResponseEntity<?> res) {
        if (idem == null || idem.isBlank()) return;
        Cached c = new Cached(res.getStatusCode().value(), res.getHeaders(), res.getBody());
        cache.put(key(method, path, idem), c);
    }
    public record Cached(int status, org.springframework.http.HttpHeaders headers, Object body) {}
}