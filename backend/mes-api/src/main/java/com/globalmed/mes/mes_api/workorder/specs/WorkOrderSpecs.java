// src/main/java/com/globalmed/mes/mes_api/workorder/WorkOrderSpecs.java
package com.globalmed.mes.mes_api.workorder.specs;

import com.globalmed.mes.mes_api.code.CodeEntity;
import com.globalmed.mes.mes_api.equipstatus.domain.EquipmentEntity;
import com.globalmed.mes.mes_api.item.ItemEntity;
import com.globalmed.mes.mes_api.process.domain.ProcessEntity;
import com.globalmed.mes.mes_api.workorder.domain.WorkOrderEntity;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

public final class WorkOrderSpecs {
    private WorkOrderSpecs(){}

    // ✅ fetch join 추가용 spec
    public static Specification<WorkOrderEntity> withFetchJoins() {
        return (root, query, cb) -> {
            // 중복 방지 (count 쿼리일 땐 fetch join 쓰면 에러남)
            if (query.getResultType() != Long.class) {
                root.fetch("itemId");
                root.fetch("processId");
                root.fetch("equipmentId").fetch("workcenter");
                root.fetch("statusCode");
            }
            return cb.conjunction();
        };
    }

    public static Specification<WorkOrderEntity> workOrderNumberContains(String workOrderNumber) {
        return (root, q, cb) -> {
            if (!StringUtils.hasText(workOrderNumber)) return cb.conjunction();
            return cb.like(cb.lower(root.get("workOrderNumber")), "%" + workOrderNumber + "%");
        };
    }

    public static Specification<WorkOrderEntity> itemNameContains(String itemName) {
        return (root, q, cb) -> {
            if (!StringUtils.hasText(itemName)) return cb.conjunction();
            Join<WorkOrderEntity, ItemEntity> itemJoin = root.join("itemId"); // WorkOrderEntity 필드명
            return cb.like(cb.lower(itemJoin.get("itemName")), "%" + itemName + "%");
        };
    }

    public static Specification<WorkOrderEntity> processNameContains(String processName) {
        return (root, q, cb) -> {
            if (!StringUtils.hasText(processName)) return cb.conjunction();
            Join<WorkOrderEntity, ProcessEntity> processJoin = root.join("processId"); // WorkOrderEntity 필드명
            return cb.like(cb.lower(processJoin.get("processName")), "%" + processName + "%");
        };
    }

    public static Specification<WorkOrderEntity> equipmentNameContains(String equipmentName) {
        return (root, q, cb) -> {
            if (!StringUtils.hasText(equipmentName)) return cb.conjunction();
            Join<WorkOrderEntity, EquipmentEntity> eqpJoin = root.join("equipmentId"); // WorkOrderEntity 필드명
            return cb.like(cb.lower(eqpJoin.get("equipmentName")), "%" + equipmentName + "%");
        };
    }

    public static Specification<WorkOrderEntity> statusEquals(String statusCode) {
        return (root, q, cb) -> {
            if (statusCode == null || statusCode.isBlank()) return cb.conjunction();
            Join<WorkOrderEntity, CodeEntity> st = root.join("statusCode"); // 연관명 'statusCode'
            return cb.equal(st.get("code"), statusCode);                    // CodeEntity의 'code'
        };
    }

    public static Specification<WorkOrderEntity> startBetween(LocalDateTime from, LocalDateTime to) {
        return (root, q, cb) -> {
            if (from == null && to == null) return cb.conjunction();
            if (from != null && to != null) return cb.between(root.get("startTs"), from, to); // 필드명 'startTs'
            if (from != null) return cb.greaterThanOrEqualTo(root.get("startTs"), from);
            return cb.lessThan(root.get("startTs"), to);
        };
    }
}