package com.globalmed.mes.mes_api.common;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * 시간대 변환 규칙
 * - attachKst: 값이 이미 KST 시각으로 읽혀온 LDT에 +09:00 오프셋만 '라벨링' (순간 변환 없음)
 * - convertUtcToKst: 진짜 UTC 시각(LDT)을 KST로 '순간 변환' (+09:00)
 */
public final class DateTimeMapper {
    private static final ZoneOffset UTC = ZoneOffset.UTC;
    private static final ZoneOffset KST = ZoneOffset.ofHours(9);

    private DateTimeMapper() {}

    // created_at / modified_at 처럼 '이미 KST로 읽혀오는' 값에 사용
    public static OffsetDateTime attachKst(LocalDateTime ldt) {
        if (ldt == null) return null;
        return OffsetDateTime.of(ldt, KST);
    }

    // 진짜 UTC로 읽혀오는 값을 KST로 변환해야 할 때만 사용
    public static OffsetDateTime convertUtcToKst(LocalDateTime ldt) {
        if (ldt == null) return null;
        return ldt.atOffset(UTC).withOffsetSameInstant(KST);
    }
}