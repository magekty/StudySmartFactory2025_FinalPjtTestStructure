// src/main/java/com/globalmed/mes/mes_api/cursor/domain/SyncCursorEntity.java
package com.globalmed.mes.mes_api.cursor.domain;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;
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

    // DB DATETIME ←→ UTC LocalDateTime 보장
    @Convert(converter = UtcLocalDateTimeConverter.class)
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

    // 엔티티 내부에 두는 UTC 컨버터(추가 파일 없이 적용)
    @Converter(autoApply = false)
    public static class UtcLocalDateTimeConverter implements AttributeConverter<LocalDateTime, Timestamp> {
        @Override
        public Timestamp convertToDatabaseColumn(LocalDateTime attribute) {
            if (attribute == null) return Timestamp.from(java.time.Instant.EPOCH);
            return Timestamp.from(attribute.atOffset(ZoneOffset.UTC).toInstant());
        }
        @Override
        public LocalDateTime convertToEntityAttribute(Timestamp dbData) {
            if (dbData == null) return LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);
            return LocalDateTime.ofInstant(dbData.toInstant(), ZoneOffset.UTC);
        }
    }
}