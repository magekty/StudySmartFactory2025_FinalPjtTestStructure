package com.factory_dynamics.erp.erp_server.bom;

import com.factory_dynamics.erp.erp_server.common.BizException;
import com.factory_dynamics.erp.erp_server.common.Uuids;
import com.factory_dynamics.erp.erp_server.product.Product;
import com.factory_dynamics.erp.erp_server.product.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
@Transactional
public class BomService {

    private final BomHeaderRepository headerRepo;
    private final BomLineRepository lineRepo;
    private final ProductRepository productRepo;

    public BomService(BomHeaderRepository headerRepo, BomLineRepository lineRepo, ProductRepository productRepo) {
        this.headerRepo = headerRepo;
        this.lineRepo = lineRepo;
        this.productRepo = productRepo;
    }

    public BomHeader createHeader(String productId, String revision, boolean active, LocalDate from, LocalDate to, String note, String actor) {
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new BizException(HttpStatus.NOT_FOUND, "제품 없음: " + productId));

        BomHeader header = BomHeader.builder()
                .id(Uuids.newId())
                .product(product)
                .revision(revision)
                .active(active)
                .effectiveFrom(from)
                .effectiveTo(to)
                .note(note)
                .build();
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        header.setCreatedAt(now);
        header.setModifiedAt(now);
        header.setCreatedBy(actor);
        header.setModifiedBy(actor);
        return headerRepo.save(header);
    }

    public List<BomLine> listLines(String bomId) {
        return lineRepo.findByBom_IdAndDeletedFalse(bomId);
    }
}