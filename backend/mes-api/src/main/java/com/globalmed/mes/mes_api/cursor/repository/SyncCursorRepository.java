// src/main/java/com/globalmed/mes/mes_api/cursor/repository/SyncCursorRepository.java
package com.globalmed.mes.mes_api.cursor.repository;

import com.globalmed.mes.mes_api.cursor.domain.SyncCursorEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public interface SyncCursorRepository extends JpaRepository<SyncCursorEntity, String> {

    default Instant get(String key) {
        return findById(key)
                .map(SyncCursorRepository::toInstant)
                .orElse(Instant.EPOCH);
    }

    default void set(String key, Instant newUpdatedAtUtc) {
        var entity = findById(key).orElseGet(() -> SyncCursorEntity.initAtEpoch(key));
        entity.setLastSyncedAt(fromInstant(newUpdatedAtUtc));
        save(entity);
    }

    private static Instant toInstant(SyncCursorEntity e) {
        LocalDateTime dt = e.getLastSyncedAt();
        return (dt == null) ? Instant.EPOCH : dt.toInstant(ZoneOffset.UTC);
    }

    private static LocalDateTime fromInstant(Instant i) {
        return LocalDateTime.ofInstant((i == null ? Instant.EPOCH : i), ZoneOffset.UTC);
    }
}