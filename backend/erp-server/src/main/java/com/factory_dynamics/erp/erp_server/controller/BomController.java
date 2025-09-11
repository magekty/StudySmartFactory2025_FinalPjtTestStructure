package com.factory_dynamics.erp.erp_server.controller;

import com.factory_dynamics.erp.erp_server.domain.BomHeader;
import com.factory_dynamics.erp.erp_server.domain.BomLine;
import com.factory_dynamics.erp.erp_server.service.BomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/boms")
public class BomController {

    private final BomService bomService;

    @PostMapping("/header")
    public ResponseEntity<BomHeader> createBomHeader(@RequestBody BomHeader bomHeader) {
        try {
            BomHeader createdBomHeader = bomService.createBom(bomHeader);
            return new ResponseEntity<>(createdBomHeader, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/line")
    public ResponseEntity<BomLine> createBomLine(@RequestBody BomLine bomLine) {
        try {
            BomLine createdBomLine = bomService.createBomLine(bomLine);
            return new ResponseEntity<>(createdBomLine, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{bomId}/header")
    public ResponseEntity<BomHeader> getBomHeaderById(@PathVariable Integer bomId) {
        Optional<BomHeader> bomHeader = bomService.getBomHeaderById(bomId);
        return bomHeader.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/{bomId}/lines")
    public ResponseEntity<List<BomLine>> getBomLinesByBomHeaderId(@PathVariable Integer bomId) {
        List<BomLine> bomLines = bomService.getBomLinesByBomHeaderId(bomId);
        if (bomLines.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(bomLines, HttpStatus.OK);
    }
}