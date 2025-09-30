package com.globalmed.mes.mes_api.production.domain;

import com.globalmed.mes.mes_api.code.CodeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_production_log")
@Getter
@Setter
public class ProductionLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    @Column(name = "work_order_id", length = 36, nullable = false)
    private String workOrderId;

    @Column(name = "equipment_id", length = 36, nullable = false)
    private String equipmentId;

    @Column(name = "process_id", length = 36, nullable = false)
    private String processId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_type", nullable = false)
    private CodeEntity eventType; 
    // tb_code(PROD_EVENT: START/END/GOODQTY/DEFECTQTY/DOWNTIME)
    //('PROD_EVENT','START','시작','Y',1,'seed'),
    //('PROD_EVENT','END','종료','Y',2,'seed'),
    //('PROD_EVENT','GOODQTY','양품수','Y',3,'seed'),
    //('PROD_EVENT','DEFECTQTY','불량수','Y',4,'seed'),
    //('PROD_EVENT','DOWNTIME','비가동','Y',5,'seed')

    @Column(name = "event_timestamp", nullable = false)
    private LocalDateTime eventTimestamp;

    @Column(name = "event_value", nullable = false, precision = 10, scale = 4)
    private BigDecimal eventValue = BigDecimal.ZERO;
}
