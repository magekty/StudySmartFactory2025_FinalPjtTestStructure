package com.factory_dynamics.erp.erp_server.mes_adapter.dto;

// ProductionPlanDto.java (MES 전송용 DTO)
import java.math.BigDecimal;
import java.time.LocalDate;

public class ProductionPlanDto {
    private String planId; // MES에서는 plan_id, ERP와 동일한 UUID 사용
    private String planCode; // MES에서는 plan_number
    private String productId; // MES에서는 item_id
    private BigDecimal qty; // MES에서는 target_qty
    private LocalDate startDate;
    private LocalDate endDate;
    private String createdBy;

    // 생성자, Getter, Setter, toString... (Lombok 사용 권장)

    // 예시: Builder 패턴 (추가적인 코드 생략)
    public ProductionPlanDto(String planId, String planCode, String productId, BigDecimal qty, LocalDate startDate, LocalDate endDate, String createdBy) {
        this.planId = planId;
        this.planCode = planCode;
        this.productId = productId;
        this.qty = qty;
        this.startDate = startDate;
        this.endDate = endDate;
        this.createdBy = createdBy;
    }

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId;
    }

    public String getPlanCode() {
        return planCode;
    }

    public void setPlanCode(String planCode) {
        this.planCode = planCode;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public BigDecimal getQty() {
        return qty;
    }

    public void setQty(BigDecimal qty) {
        this.qty = qty;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}