package com.factory_dynamics.erp.erp_server.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, String> {
    Optional<Product> findByProductCodeAndDeletedFalse(String productCode);
    @Query("""
      select p from Product p
      where (:status = 'ALL'
             or (:status = 'ACTIVE' and p.deleted = false)
             or (:status = 'INACTIVE' and p.deleted = true))
        and (
          lower(p.name) like lower(concat('%', :q, '%'))
          or lower(p.productCode) like lower(concat('%', :q, '%'))
        )
    """)
    Page<Product> searchByNameOrCodeLike(@Param("q") String q,
                                         @Param("status") String status,
                                         Pageable pageable);
}