package com.globalmed.mes.mes_api.performance.specs;

import com.globalmed.mes.mes_api.performance.domain.ProductionPerformanceEntity;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public final class PerformanceSpecs {
    private PerformanceSpecs(){}

    public static Specification<ProductionPerformanceEntity> equipmentIdEquals(String equipmentId) {
        return (root, q, cb) -> equipmentId == null || equipmentId.isBlank()
                ? cb.conjunction()
                : cb.equal(root.get("equipmentId"), equipmentId);
    }

    public static Specification<ProductionPerformanceEntity> startBetween(LocalDateTime from, LocalDateTime to) {
        return (root, q, cb) -> {
            if (from == null && to == null) return cb.conjunction();
            if (from != null && to != null) return cb.between(root.get("startTime"), from, to);
            if (from != null) return cb.greaterThanOrEqualTo(root.get("startTime"), from);
            return cb.lessThan(root.get("startTime"), to);
        };
    }
}