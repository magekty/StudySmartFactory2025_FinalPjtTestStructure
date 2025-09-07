// src/main/java/com/globalmed/mes/mes_api/bom/domain/BomHeaderEntity.java
package com.globalmed.mes.mes_api.bom.domain;

import com.globalmed.mes.mes_api.common.jpa.OffsetDateTimeUtcConverter;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "tb_bom_header",
        uniqueConstraints = @UniqueConstraint(name = "uq_bom_header_nat", columnNames = {"item_id","revision","alt_code"}))
public class BomHeaderEntity {

    @Id
    @Column(name = "bom_id", length = 50, nullable = false)
    private String bomId;

    @Column(name = "item_id", length = 36, nullable = false)
    private String itemId;

    @Column(name = "revision", length = 20, nullable = false)
    private String revision;

    @Column(name = "alt_code", length = 20, nullable = false)
    private String altCode;

    @Convert(converter = OffsetDateTimeUtcConverter.class)
    @Column(name = "eff_from", nullable = false)
    private OffsetDateTime effFromUtc;

    @Convert(converter = OffsetDateTimeUtcConverter.class)
    @Column(name = "eff_to")
    private OffsetDateTime effToUtc;

    @Column(name = "description", length = 255)
    private String description;

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