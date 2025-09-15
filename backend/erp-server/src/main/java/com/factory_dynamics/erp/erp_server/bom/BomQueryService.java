package com.factory_dynamics.erp.erp_server.bom;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class BomQueryService {

    private final BomLineRepository bomLineRepository;

    public BomQueryService(BomLineRepository bomLineRepository) {
        this.bomLineRepository = bomLineRepository;
    }

    public List<BomLineResponse> getLines(String bomId) {
        return bomLineRepository.findWithComponentByBomId(bomId)
                .stream()
                .map(BomLineResponse::from)
                .toList();
    }
}