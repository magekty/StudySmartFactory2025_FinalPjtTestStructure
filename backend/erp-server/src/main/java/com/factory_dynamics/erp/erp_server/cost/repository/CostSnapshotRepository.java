package com.factory_dynamics.erp.erp_server.cost.repository;

import com.factory_dynamics.erp.erp_server.cost.entity.CostSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CostSnapshotRepository extends JpaRepository<CostSnapshot, String> { }