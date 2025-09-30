// src/main/java/.../common/DateTimeMapper.java
package com.globalmed.mes.mes_api.common;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public final class DateTimeMapper {
    private static final ZoneOffset KST = ZoneOffset.ofHours(9);
    private DateTimeMapper(){}

    // created_at/modified_at 전용: 순간 변환 없이 +09:00 오프셋만 부착
    public static OffsetDateTime attachKst(LocalDateTime ldt) {
        return (ldt == null) ? null : OffsetDateTime.of(ldt, KST);
    }
}