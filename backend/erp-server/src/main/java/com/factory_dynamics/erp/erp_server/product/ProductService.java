package com.factory_dynamics.erp.erp_server.product;

import com.factory_dynamics.erp.erp_server.common.Uuids;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
@Transactional
public class ProductService {

    private final ProductRepository repo;

    public ProductService(ProductRepository repo) {
        this.repo = repo;
    }

    public Product create(ProductCreateRequest req, String actor) {
        Product p = Product.builder()
                .id(Uuids.newId())
                .productCode(req.productCode())
                .name(req.name())
                .type(req.type())
                .unit(req.unit())
                .description(req.description())
                .build();
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        p.setCreatedAt(now);
        p.setModifiedAt(now);
        p.setCreatedBy(actor);
        p.setModifiedBy(actor);
        return repo.save(p);
    }
}