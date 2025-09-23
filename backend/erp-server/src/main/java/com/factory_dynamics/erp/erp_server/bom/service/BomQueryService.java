package com.factory_dynamics.erp.erp_server.bom.service;

import com.factory_dynamics.erp.erp_server.bom.entity.BomLine;
import com.factory_dynamics.erp.erp_server.bom.BomLineResponse;
import com.factory_dynamics.erp.erp_server.bom.repository.BomHeaderRepository;
import com.factory_dynamics.erp.erp_server.bom.repository.BomLineRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.factory_dynamics.erp.erp_server.bom.dto.BomTreeNodeResponse;
import com.factory_dynamics.erp.erp_server.common.BizException;
import org.springframework.http.HttpStatus;

import java.util.*;

@Service

public class BomQueryService {

    private final BomHeaderRepository headerRepo;
    private final BomLineRepository lineRepo;

    public BomQueryService(BomHeaderRepository headerRepo, BomLineRepository lineRepo) {
        this.headerRepo = headerRepo;
        this.lineRepo = lineRepo;
    }


    @Transactional(readOnly = true)
    public List<BomLineResponse> getLines(String bomId) {
        return lineRepo.findLinesWithComponent(bomId)
                .stream()
                .map(BomLineResponse::from)
                .toList();
    }
    public List<BomTreeNodeResponse> getTree(String bomId) {
        var header = headerRepo.findById(bomId)
                .orElseThrow(() -> new BizException(HttpStatus.NOT_FOUND, "BOM 헤더 없음: " + bomId));

        var lines = lineRepo.findLinesWithComponent(header.getId());

        Map<String, List<BomLine>> byParent = new HashMap<>();
        for (var l : lines) {
            var key = l.getParent() == null ? "ROOT" : l.getParent().getId();
            byParent.computeIfAbsent(key, k -> new ArrayList<>()).add(l);
        }

        List<BomTreeNodeResponse> roots = new ArrayList<>();
        for (var rootLine : byParent.getOrDefault("ROOT", List.of())) {
            roots.add(buildNode(rootLine, byParent));
        }
        return roots;
    }

    private BomTreeNodeResponse buildNode(BomLine l, Map<String, List<BomLine>> byParent) {
        var n = new BomTreeNodeResponse();
        n.bomLineId = l.getId();
        n.parentLineId = l.getParent() == null ? null : l.getParent().getId();
        n.componentProductId = l.getComponent().getId();
        n.componentCode = l.getComponent().getProductCode();
        n.componentName = l.getComponent().getName();
        n.qty = l.getQty();
        n.scrapRate = l.getScrapRate();
        n.note = l.getNote();

        for (var child : byParent.getOrDefault(l.getId(), List.of())) {
            n.children.add(buildNode(child, byParent));
        }
        return n;
    }

    public void assertNoChildren(String lineId) {
        var children = lineRepo.findChildren(lineId);
        if (!children.isEmpty()) {
            throw new BizException(HttpStatus.BAD_REQUEST, "자식 라인이 있어 삭제 불가: " + lineId);
        }
    }
}