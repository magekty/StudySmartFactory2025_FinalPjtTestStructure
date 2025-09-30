package com.globalmed.mes.mes_api.employee.cert.repository;

import com.globalmed.mes.mes_api.employee.cert.domain.CertEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CertRepo extends JpaRepository<CertEntity, Long> {
    Optional<CertEntity> findByCertCode(String certCode);
}
