// src/main/java/com/globalmed/mes/mes_api/cursor/domain/SyncCursor.java
package com.globalmed.mes.mes_api.cursor.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "tb_sync_cursor")
public class SyncCursor {

    @Id
    @Column(name = "cursor_key", length = 50, nullable = false)
    private String cursorKey;

    @Column(name = "last_synced_at", nullable = false)
    private OffsetDateTime lastSyncedAt;

    public static SyncCursor of(String key, OffsetDateTime ts) {
        SyncCursor c = new SyncCursor();
        c.cursorKey = key;
        c.lastSyncedAt = (ts != null ? ts : OffsetDateTime.of(1970,1,1,0,0,0,0, ZoneOffset.UTC));
        return c;
    }

    public static SyncCursor initAtEpoch(String key) {
        return of(key, OffsetDateTime.of(1970,1,1,0,0,0,0, ZoneOffset.UTC));
    }
}