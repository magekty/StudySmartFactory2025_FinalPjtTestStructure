package com.globalmed.mes.mes_api.code;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CodeRepo extends JpaRepository<CodeEntity, Long> {
    Optional<CodeEntity> findByGroupCodeAndCodeAndUseYn(String groupCode, String code, char useYn);
}