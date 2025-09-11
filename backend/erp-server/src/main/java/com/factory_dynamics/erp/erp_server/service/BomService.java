package com.factory_dynamics.erp.erp_server.service;

import com.factory_dynamics.erp.erp_server.domain.BomHeader;
import com.factory_dynamics.erp.erp_server.domain.BomLine;
import com.factory_dynamics.erp.erp_server.domain.Product;
import com.factory_dynamics.erp.erp_server.repository.BomHeaderRepository;
import com.factory_dynamics.erp.erp_server.repository.BomLineRepository;
import com.factory_dynamics.erp.erp_server.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BomService {

    private final BomHeaderRepository bomHeaderRepository;
    private final BomLineRepository bomLineRepository;
    private final ProductRepository productRepository;

    @Transactional
    public BomHeader createBom(BomHeader bomHeader) {
        // 필수 값 검증 (예: 제품 ID)
        if (bomHeader.getProduct() == null || bomHeader.getProduct().getProductId() == null) {
            throw new IllegalArgumentException("Product ID is required to create a BOM.");
        }

        // 제품 존재 여부 확인
        Optional<Product> product = productRepository.findById(bomHeader.getProduct().getProductId());
        if (product.isEmpty()) {
            throw new IllegalArgumentException("Product not found with ID: " + bomHeader.getProduct().getProductId());
        }
        bomHeader.setProduct(product.get());

        return bomHeaderRepository.save(bomHeader);
    }

    @Transactional
    public BomLine createBomLine(BomLine bomLine) {
        // 필수 값 및 관계 검증
        if (bomLine.getBomHeader() == null || bomLine.getBomHeader().getBomId() == null) {
            throw new IllegalArgumentException("BomHeader ID is required for BomLine.");
        }

        // BomHeader 존재 여부 확인
        Optional<BomHeader> bomHeader = bomHeaderRepository.findById(bomLine.getBomHeader().getBomId());
        if (bomHeader.isEmpty()) {
            throw new IllegalArgumentException("BomHeader not found with ID: " + bomLine.getBomHeader().getBomId());
        }
        bomLine.setBomHeader(bomHeader.get());

        // 부모 라인이 존재하는 경우, 부모 라인 존재 여부 확인
        if (bomLine.getParentLine() != null && bomLine.getParentLine().getLineId() != null) {
            Optional<BomLine> parentLine = bomLineRepository.findById(bomLine.getParentLine().getLineId());
            if (parentLine.isEmpty()) {
                throw new IllegalArgumentException("Parent BomLine not found with ID: " + bomLine.getParentLine().getLineId());
            }
            bomLine.setParentLine(parentLine.get());
        }

        return bomLineRepository.save(bomLine);
    }

    public Optional<BomHeader> getBomHeaderById(Integer bomId) {
        return bomHeaderRepository.findById(bomId);
    }

    public List<BomLine> getBomLinesByBomHeaderId(Integer bomId) {
        return bomLineRepository.findHierarchicalBomLines(bomId);
    }
}