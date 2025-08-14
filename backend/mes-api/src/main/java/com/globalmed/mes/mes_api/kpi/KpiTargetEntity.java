package com.globalmed.mes.mes_api.kpi;

import jakarta.persistence.*;
import lombok.Getter; import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "tb_kpi_target",
        uniqueConstraints = @UniqueConstraint(name="uk_kpi_target_date_eqp_proc_item",
                columnNames = {"kpi_date","equipment_id","process_id","item_id"}))
@Getter @Setter
public class KpiTargetEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "target_id")
    private Long targetId;

    @Column(name = "kpi_date", nullable = false)
    private LocalDate kpiDate;

    @Column(name = "equipment_id", length = 36, nullable = false)
    private String equipmentId;

    @Column(name = "process_id", length = 36, nullable = false)
    private String processId;

    @Column(name = "item_id", length = 36, nullable = false)
    private String itemId;

    @Column(name = "target_oee", precision = 5, scale = 2, nullable = false)
    private BigDecimal targetOee;

    @Column(name = "target_productivity", precision = 10, scale = 4, nullable = false)
    private BigDecimal targetProductivity;

    @Column(name = "target_yield", precision = 5, scale = 2, nullable = false)
    private BigDecimal targetYield;
}