package com.factory_dynamics.erp.erp_server.repository;

import com.factory_dynamics.erp.erp_server.domain.BomHeader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BomHeaderRepository extends JpaRepository<BomHeader, Integer> {
    // Product 엔티티의 productId를 기준으로 BOM Header 조회
    List<BomHeader> findByProduct_ProductId(Integer productId);
}