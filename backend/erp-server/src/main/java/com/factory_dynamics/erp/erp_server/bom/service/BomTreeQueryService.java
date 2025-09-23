// 9) Java BE - 트리도 서비스에서 모두 로딩 후 DTO 구성 (no session 방지)
package com.factory_dynamics.erp.erp_server.bom.service;

import com.factory_dynamics.erp.erp_server.bom.entity.BomLine;
import com.factory_dynamics.erp.erp_server.bom.dto.BomTreeNodeResponse;
import com.factory_dynamics.erp.erp_server.bom.repository.BomHeaderRepository;
import com.factory_dynamics.erp.erp_server.bom.repository.BomLineRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class BomTreeQueryService {

    private final BomHeaderRepository headerRepo;
    private final BomLineRepository lineRepo;

    public BomTreeQueryService(BomHeaderRepository headerRepo, BomLineRepository lineRepo) {
        this.headerRepo = headerRepo;
        this.lineRepo = lineRepo;
    }

    @Transactional(readOnly = true)
    public List<BomTreeNodeResponse> getTree(String bomId) {
        headerRepo.findById(bomId).orElseThrow();

        var lines = lineRepo.findLinesWithComponent(bomId);
        Map<String, List<BomLine>> byParent = new HashMap<>();
        for (var l : lines) {
            var key = l.getParent() == null ? "ROOT" : l.getParent().getId();
            byParent.computeIfAbsent(key, k -> new ArrayList<>()).add(l);
        }

        List<BomTreeNodeResponse> roots = new ArrayList<>();
        for (var root : byParent.getOrDefault("ROOT", List.of())) {
            roots.add(build(root, byParent));
        }
        return roots;
    }

    private BomTreeNodeResponse build(BomLine l, Map<String, List<BomLine>> byParent) {
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
            n.children.add(build(child, byParent));
        }
        return n;
    }
}