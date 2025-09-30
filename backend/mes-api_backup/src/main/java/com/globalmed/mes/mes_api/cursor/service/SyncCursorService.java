// src/main/java/com/globalmed/mes/mes_api/cursor/service/SyncCursorService.java
package com.globalmed.mes.mes_api.cursor.service;

import com.globalmed.mes.mes_api.cursor.domain.SyncCursorEntity;
import com.globalmed.mes.mes_api.cursor.repository.SyncCursorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;

@Service
@RequiredArgsConstructor
public class SyncCursorService {

    private final SyncCursorRepository repo;
    private static final ZoneOffset Z = ZoneOffset.UTC;

    @Transactional(readOnly = true)
    public Instant getInstant(String key) {
        return repo.findById(key)
                .map(SyncCursorEntity::getLastSyncedAt)
                .map(dt -> dt.toInstant(Z))
                .orElse(Instant.EPOCH);
    }

    @Transactional
    public void setInstant(String key, Instant newUpdatedAt) {
        LocalDateTime dt = LocalDateTime.ofInstant(newUpdatedAt, Z);
        upsert(key, dt);
    }

    @Transactional(readOnly = true)
    public OffsetDateTime getOffset(String key) {
        return repo.findById(key)
                .map(SyncCursorEntity::getLastSyncedAt)
                .map(dt -> dt.atOffset(Z))
                .orElse(Instant.EPOCH.atOffset(Z));
    }

    @Transactional
    public void setOffset(String key, OffsetDateTime odt) {
        LocalDateTime dt = odt.withOffsetSameInstant(Z).toLocalDateTime();
        upsert(key, dt);
    }

    @Transactional(readOnly = true)
    public LocalDateTime getLocalUtc(String key) {
        return repo.findById(key)
                .map(SyncCursorEntity::getLastSyncedAt)
                .orElse(LocalDateTime.ofEpochSecond(0, 0, Z));
    }

    @Transactional
    public void setLocalUtc(String key, LocalDateTime utc) {
        upsert(key, utc);
    }

    private void upsert(String key, LocalDateTime utc) {
        SyncCursorEntity cur = repo.findById(key).orElseGet(() -> SyncCursorEntity.initAtEpoch(key));
        cur.setLastSyncedAt(utc != null ? utc : LocalDateTime.ofEpochSecond(0, 0, Z));
        repo.save(cur);
    }
}