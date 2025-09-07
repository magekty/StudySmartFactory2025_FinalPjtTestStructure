// src/main/java/com/globalmed/mes/mes_api/bom/domain/BomLineEntity.java
package com.globalmed.mes.mes_api.bom.domain;

import com.globalmed.mes.mes_api.common.jpa.OffsetDateTimeUtcConverter;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "tb_bom_line",
        uniqueConstraints = @UniqueConstraint(name = "uq_bom_line_nat", columnNames = {"bom_id","line_no"}))
public class BomLineEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "line_id")
    private Long lineId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bom_id", nullable = false)
    private BomHeaderEntity header;

    @Column(name = "line_no", nullable = false)
    private Integer lineNo;

    @Column(name = "component_id", length = 36, nullable = false)
    private String componentId;

    @Column(name = "qty", precision = 18, scale = 6, nullable = false)
    private BigDecimal qty;

    @Column(name = "uom", length = 20, nullable = false)
    private String uom;

    @Column(name = "scrap_rate", precision = 9, scale = 6, nullable = false)
    private BigDecimal scrapRate;

    @Builder.Default
    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    @Convert(converter = OffsetDateTimeUtcConverter.class)
    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    @Column(name = "created_by", length = 50, nullable = false)
    private String createdBy;

    @Convert(converter = OffsetDateTimeUtcConverter.class)
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "modified_by", length = 50)
    private String modifiedBy;

    @Convert(converter = OffsetDateTimeUtcConverter.class)
    @Column(name = "modified_at")
    private OffsetDateTime modifiedAt;
}