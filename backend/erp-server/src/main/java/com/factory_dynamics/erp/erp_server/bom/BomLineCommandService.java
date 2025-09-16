// src/main/java/com/factory_dynamics/erp/erp_server/bom/BomLineCommandService.java
package com.factory_dynamics.erp.erp_server.bom;

import com.factory_dynamics.erp.erp_server.common.BizException;
import com.factory_dynamics.erp.erp_server.common.Uuids;
import com.factory_dynamics.erp.erp_server.product.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.Set;

@Service
@Transactional
public class BomLineCommandService {

    private final BomHeaderRepository headerRepo;
    private final BomLineRepository lineRepo;
    private final ProductRepository productRepo;
    private final BomQueryService queryService;

    public BomLineCommandService(BomHeaderRepository headerRepo,
                                 BomLineRepository lineRepo,
                                 ProductRepository productRepo,
                                 BomQueryService queryService) {
        this.headerRepo = headerRepo;
        this.lineRepo = lineRepo;
        this.productRepo = productRepo;
        this.queryService = queryService;
    }

    public BomLineResponse addLine(BomLineCreateRequest req, String actor) {
        var header = headerRepo.findById(req.bomId())
                .orElseThrow(() -> new BizException(HttpStatus.NOT_FOUND, "BOM 헤더 없음"));
        var component = productRepo.findById(req.componentProductId())
                .orElseThrow(() -> new BizException(HttpStatus.NOT_FOUND, "구성품 제품 없음"));

        BomLine parent = null;
        if (req.parentLineId() != null) {
            parent = lineRepo.findById(req.parentLineId())
                    .orElseThrow(() -> new BizException(HttpStatus.NOT_FOUND, "부모 라인 없음"));
            if (!parent.getBom().getId().equals(header.getId())) {
                throw new BizException(HttpStatus.BAD_REQUEST, "부모 라인의 BOM이 다름");
            }
        }

        if (isCycle(header.getId(), parent, component.getId())) {
            throw new BizException(HttpStatus.CONFLICT, "사이클 감지: 상위에 동일 구성 존재");
        }

        var line = BomLine.builder()
                .id(Uuids.newId())
                .bom(header)
                .parent(parent)
                .component(component)
                .qty(req.qty())
                .scrapRate(req.scrapRate())
                .note(req.note())
                .build();

        var now = LocalDateTime.now(ZoneOffset.UTC);
        line.setCreatedAt(now);
        line.setModifiedAt(now);
        line.setCreatedBy(actor);
        line.setModifiedBy(actor);

        lineRepo.save(line);
        return BomLineResponse.from(line);
    }

    public void removeLine(String lineId, String actor) {
        var line = lineRepo.findById(lineId)
                .orElseThrow(() -> new BizException(HttpStatus.NOT_FOUND, "라인 없음"));

        queryService.assertNoChildren(lineId); // 자식 있으면 삭제 불가(안전형 정책)

        line.setDeleted(true);
        line.setDeletedAt(LocalDateTime.now(ZoneOffset.UTC));
        line.setModifiedAt(LocalDateTime.now(ZoneOffset.UTC));
        line.setModifiedBy(actor);
        lineRepo.save(line);
    }

    private boolean isCycle(String bomId, BomLine parent, String componentProductId) {
        Set<String> path = new HashSet<>();
        BomLine cur = parent;
        while (cur != null) {
            path.add(cur.getComponent().getId());
            cur = cur.getParent();
        }
        return path.contains(componentProductId);
    }
}