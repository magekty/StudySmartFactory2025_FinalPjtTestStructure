package com.globalmed.mes.mes_api.performance.specs;

import com.globalmed.mes.mes_api.item.ItemEntity;
import com.globalmed.mes.mes_api.process.domain.ProcessEntity;
import com.globalmed.mes.mes_api.equipstatus.domain.EquipmentEntity;
import com.globalmed.mes.mes_api.performance.domain.ProductionPerformanceEntity;
import com.globalmed.mes.mes_api.workorder.domain.WorkOrderEntity;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.Join;
import java.time.LocalDateTime;

public final class PerformanceSpecs {
    private PerformanceSpecs(){}

    public static Specification<ProductionPerformanceEntity> equipmentIdEquals(String equipmentId) {
        return (root, q, cb) -> {
            if (!StringUtils.hasText(equipmentId)) return cb.conjunction();
            Join<ProductionPerformanceEntity, EquipmentEntity> equipmentJoin = root.join("equipment");
            return cb.equal(equipmentJoin.get("equipmentId"), equipmentId);
        };
    }

    public static Specification<ProductionPerformanceEntity> startBetween(LocalDateTime from, LocalDateTime to) {
        return (root, q, cb) -> {
            if (from == null && to == null) return cb.conjunction();
            if (from != null && to != null) return cb.between(root.get("startTime"), from, to);
            if (from != null) return cb.greaterThanOrEqualTo(root.get("startTime"), from);
            return cb.lessThan(root.get("startTime"), to);
        };
    }

    /**
     * 작업지시번호(workOrderName)를 LIKE 검색하는 Specification
     */
    public static Specification<ProductionPerformanceEntity> workOrderNumberLike(String workOrderNumber) {
        return (root, q, cb) -> {
            if (!StringUtils.hasText(workOrderNumber)) return cb.conjunction();
            // Join `ProductionPerformanceEntity` to `WorkOrderEntity` using the 'workOrder' field
            Join<ProductionPerformanceEntity, WorkOrderEntity> woJoin = root.join("workOrder");
            return cb.like(woJoin.get("workOrderNumber"), "%" + workOrderNumber + "%");
        };
    }

    /**
     * 품목명(itemName)을 LIKE 검색하는 Specification
     */
    public static Specification<ProductionPerformanceEntity> itemNameLike(String itemName) {
        return (root, q, cb) -> {
            if (!StringUtils.hasText(itemName)) return cb.conjunction();
            // **수정** : `Join`에 제네릭 타입 `(ProductionPerformanceEntity, ItemEntity)` 명시
            Join<ProductionPerformanceEntity, ItemEntity> itemJoin = root.join("item");
            return cb.like(itemJoin.get("itemName"), "%" + itemName + "%");
        };
    }

    /**
     * 공정명(processName)을 LIKE 검색하는 Specification
     */
    public static Specification<ProductionPerformanceEntity> processNameLike(String processName) {
        return (root, q, cb) -> {
            if (!StringUtils.hasText(processName)) return cb.conjunction();
            // **수정** : `Join`에 제네릭 타입 `(ProductionPerformanceEntity, ProcessEntity)` 명시
            Join<ProductionPerformanceEntity, ProcessEntity> processJoin = root.join("process");
            return cb.like(processJoin.get("processName"), "%" + processName + "%");
        };
    }

    /**
     * 설비명(equipmentName)을 LIKE 검색하는 Specification
     */
    public static Specification<ProductionPerformanceEntity> equipmentNameLike(String equipmentName) {
        return (root, q, cb) -> {
            if (!StringUtils.hasText(equipmentName)) return cb.conjunction();
            // **수정** : `Join`에 제네릭 타입 `(ProductionPerformanceEntity, EquipmentEntity)` 명시
            Join<ProductionPerformanceEntity, EquipmentEntity> equipmentJoin = root.join("equipment");
            return cb.like(equipmentJoin.get("equipmentName"), "%" + equipmentName + "%");
        };
    }

}