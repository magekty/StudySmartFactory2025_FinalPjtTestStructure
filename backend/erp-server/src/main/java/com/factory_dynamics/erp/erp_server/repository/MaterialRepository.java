package com.factory_dynamics.erp.erp_server.repository;

import com.factory_dynamics.erp.erp_server.domain.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MaterialRepository extends JpaRepository<Material, Integer> {
}