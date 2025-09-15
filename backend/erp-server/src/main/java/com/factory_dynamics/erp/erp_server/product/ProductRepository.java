package com.factory_dynamics.erp.erp_server.product;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, String> {
    Optional<Product> findByProductCodeAndDeletedFalse(String productCode);
}