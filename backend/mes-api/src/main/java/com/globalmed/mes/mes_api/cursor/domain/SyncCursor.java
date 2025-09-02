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

    // 최소/명시 팩토리: 서비스에서 new를 호출하지 않도록 제공
    public static SyncCursor of(String key, OffsetDateTime ts) {
        SyncCursor c = new SyncCursor(); // 클래스 내부라 protected 생성자 호출 가능
        c.cursorKey = key;
        c.lastSyncedAt = (ts != null ? ts : OffsetDateTime.of(1970,1,1,0,0,0,0, ZoneOffset.UTC));
        return c;
    }

    public static SyncCursor initAtEpoch(String key) {
        return of(key, OffsetDateTime.of(1970,1,1,0,0,0,0, ZoneOffset.UTC));
    }
}