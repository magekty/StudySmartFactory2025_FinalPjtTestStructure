package com.globalmed.mes.mes_api.code.Definition.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Entity
@Table(name = "tb_definition")
@Getter
@Setter
public class DefinitionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "definition_id")
    private Long definitionId;

    @Column(name = "definition_name", length = 100, nullable = false)
    private String definitionName;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "formula", length = 500, nullable = false)
    private String formula;

    @Column(name = "parameters", columnDefinition = "JSON", nullable = false)
    private String parameters;

    @Column(name = "unit", length = 20)
    private String unit;

    @Column(name = "created_by", length = 50, nullable = false)
    private String createdBy;

    @PrePersist
    void prePersist() {
        if (createdBy == null || createdBy.isBlank()) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            createdBy = (auth != null && auth.isAuthenticated())
                    ? String.valueOf(auth.getPrincipal())
                    : "system";
        }
    }
}