package com.globalmed.mes.mes_api.code.Definition.repository;

import com.globalmed.mes.mes_api.code.Definition.domain.DefinitionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DefinitionRepo extends JpaRepository<DefinitionEntity, Long> {
    Optional<DefinitionEntity> findByDefinitionName(String definitionName);
}
