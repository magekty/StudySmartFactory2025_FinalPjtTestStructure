package com.factory_dynamics.erp.erp_server.common;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class MoneyMath {
    private MoneyMath() {}
    // 내부 계산은 소수점 6자리 유지
    public static BigDecimal scale6(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v.setScale(6, RoundingMode.HALF_UP);
    }
    // 최종 금액 표시 2자리 반올림
    public static BigDecimal toDisplay2(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v.setScale(2, RoundingMode.HALF_UP);
    }
}