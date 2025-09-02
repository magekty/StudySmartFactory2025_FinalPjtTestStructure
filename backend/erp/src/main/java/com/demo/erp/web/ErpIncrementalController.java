// src/main/java/com/demo/erp/web/ErpIncrementalController.java
package com.demo.erp.web;

import com.demo.erp.service.BomService;
import com.demo.erp.service.ItemService;
import com.demo.erp.service.PlanService;
import com.demo.erp.web.dto.BomHeaderDto;
import com.demo.erp.web.dto.ItemDto;
import com.demo.erp.web.dto.PlanLineView;
import com.demo.erp.web.dto.PlanUpsertDto;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping
public class ErpIncrementalController {

    private final ItemService itemService;
    private final BomService bomService;
    private final PlanService planService;

    public ErpIncrementalController(ItemService itemService, BomService bomService, PlanService planService) {
        this.itemService = itemService;
        this.bomService = bomService;
        this.planService = planService;
    }

    @PostConstruct
    public void seed() {
        itemService.seedIfEmpty();
        bomService.seedIfEmpty();
    }

    @GetMapping("/items")
    public ResponseEntity<List<ItemDto>> items(@RequestParam(required = false) OffsetDateTime updatedSince) {
        return ResponseEntity.ok(itemService.list(updatedSince));
    }

    @GetMapping("/boms")
    public ResponseEntity<List<BomHeaderDto>> boms(@RequestParam(required = false) OffsetDateTime updatedSince) {
        return ResponseEntity.ok(bomService.list(updatedSince));
    }

    @PostMapping("/plans")
    public ResponseEntity<Void> planPost(@RequestBody @Valid PlanUpsertDto dto) {
        return planService.post(dto);
    }

    @PutMapping("/plans")
    public ResponseEntity<Void> planPut(@RequestBody @Valid PlanUpsertDto dto) {
        return planService.put(dto);
    }

    @GetMapping("/plans")
    public ResponseEntity<List<PlanLineView>> plans(@RequestParam(required = false) OffsetDateTime updatedSince,
                                                    @RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "100") int size) {
        if (size > 100) size = 100;
        return ResponseEntity.ok(planService.list(updatedSince, page, size));
    }

}