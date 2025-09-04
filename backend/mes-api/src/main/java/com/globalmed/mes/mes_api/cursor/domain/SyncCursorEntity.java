// src/main/java/com/globalmed/mes/mes_api/cursor/domain/SyncCursorEntity.java
package com.globalmed.mes.mes_api.cursor.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "tb_sync_cursor")
public class SyncCursorEntity {

    @Id
    @Column(name = "cursor_key", length = 50, nullable = false)
    private String cursorKey;

    // DATETIME ↔ LocalDateTime(UTC로 해석/저장)
    @Column(name = "last_synced_at", nullable = false, columnDefinition = "datetime")
    private LocalDateTime lastSyncedAt;

    @PrePersist
    @PreUpdate
    void normalize() {
        if (lastSyncedAt == null) {
            lastSyncedAt = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);
        }
    }

    public static SyncCursorEntity of(String key, LocalDateTime utc) {
        SyncCursorEntity c = new SyncCursorEntity();
        c.cursorKey = key;
        c.lastSyncedAt = (utc != null ? utc : LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC));
        return c;
    }

    public static SyncCursorEntity initAtEpoch(String key) {
        return of(key, LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC));
    }
}