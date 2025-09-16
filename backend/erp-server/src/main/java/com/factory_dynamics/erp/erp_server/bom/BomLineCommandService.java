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

@Service
@Transactional
public class BomLineCommandService {

    private final BomHeaderRepository headerRepo;
    private final BomLineRepository lineRepo;
    private final ProductRepository productRepo;

    public BomLineCommandService(BomHeaderRepository headerRepo,
                                 BomLineRepository lineRepo,
                                 ProductRepository productRepo) {
        this.headerRepo = headerRepo;
        this.lineRepo = lineRepo;
        this.productRepo = productRepo;
    }

    public BomLineResponse addLineWithRecycle(BomLineCreateRequest req, String actor) {
        try {
            return addLine(req, actor);
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            return upsertActiveLine(req, actor);
        }
    }

    public BomLineResponse addLine(BomLineCreateRequest req, String actor) {
        var header = headerRepo.findById(req.bomId())
                .orElseThrow(() -> new BizException(HttpStatus.NOT_FOUND, "BOM 헤더 없음"));
        var component = productRepo.findById(req.componentProductId())
                .orElseThrow(() -> new BizException(HttpStatus.NOT_FOUND, "구성품 없음"));

        BomLine parent = null;
        if (req.parentLineId() != null) {
            parent = lineRepo.findById(req.parentLineId())
                    .orElseThrow(() -> new BizException(HttpStatus.NOT_FOUND, "부모 라인 없음"));
            if (!parent.getBom().getId().equals(header.getId()))
                throw new BizException(HttpStatus.BAD_REQUEST, "부모 라인의 BOM 불일치");
        }

        var now = LocalDateTime.now(ZoneOffset.UTC);

        var line = BomLine.builder()
                .id(Uuids.newId())
                .bom(header)
                .parent(parent)
                .component(component)
                .qty(req.qty())
                .scrapRate(req.scrapRate())
                .note(req.note())
                .build();

        line.setCreatedAt(now);
        line.setModifiedAt(now);
        line.setCreatedBy(actor);
        line.setModifiedBy(actor);
        line.setDeleted(false);
        line.setActiveKey("A"); // 활성로 명시

        lineRepo.save(line);
        return BomLineResponse.from(line);
    }

    public BomLineResponse upsertActiveLine(BomLineCreateRequest req, String actor) {
        var list = lineRepo.findAllByBusinessKey(req.bomId(), req.parentLineId(), req.componentProductId());
        var deletedOne = list.stream().filter(BomLine::isDeleted).findFirst().orElse(null);
        if (deletedOne != null) {
            deletedOne.reviveAsActive(actor);
            deletedOne.setQty(req.qty());
            deletedOne.setScrapRate(req.scrapRate());
            deletedOne.setNote(req.note());
            return BomLineResponse.from(lineRepo.save(deletedOne));
        }
        throw new BizException(HttpStatus.CONFLICT, "동일 구성 요소가 이미 존재합니다.");
    }

    public void removeLine(String lineId, String actor) {
        var line = lineRepo.findById(lineId)
                .orElseThrow(() -> new BizException(HttpStatus.NOT_FOUND, "라인 없음"));

        line.markDeleted(actor); // is_deleted=true + active_key='Z'
        lineRepo.save(line);
    }
}