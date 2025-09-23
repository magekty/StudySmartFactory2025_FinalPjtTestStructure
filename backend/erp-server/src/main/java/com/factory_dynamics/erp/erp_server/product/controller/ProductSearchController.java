package com.factory_dynamics.erp.erp_server.product.controller;

import com.factory_dynamics.erp.erp_server.product.ProductListItem;
import com.factory_dynamics.erp.erp_server.product.ProductRepository;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
public class ProductSearchController {

    private final ProductRepository repo;

    public ProductSearchController(ProductRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ProductListItem>> search(@RequestParam(defaultValue = "") String q,
                                                        @RequestParam(defaultValue = "ALL") String status, // ALL|ACTIVE|INACTIVE
                                                        @RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "50") int size,
                                                        @RequestParam(defaultValue = "productCode,asc") String sort) {
        var parts = sort.split(",");
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(parts[1]), parts[0]));
        var res = repo.searchByNameOrCodeLike(q, status, pageable).map(ProductListItem::from);
        return ResponseEntity.ok(res);
    }
}