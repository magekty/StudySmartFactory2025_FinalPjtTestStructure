package com.globalmed.mes.mes_api.process.controller;

import com.globalmed.mes.mes_api.process.dto.ProcessCreationDto;
import com.globalmed.mes.mes_api.process.dto.ProcessDetailDto;
import com.globalmed.mes.mes_api.process.dto.ProcessListDto;
import com.globalmed.mes.mes_api.process.service.ProcessService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/process")
@RequiredArgsConstructor

public class ProcessController {

    private final ProcessService processService;

    @GetMapping
    public ResponseEntity<Page<ProcessListDto>> getProcesses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "processId,asc") String sort
    ) {
        Sort s = Sort.by(sort.split(",")[0]);
        if (sort.split(",").length > 1 && sort.split(",")[1].equalsIgnoreCase("desc")) {
            s = s.descending();
        }
        PageRequest pageable = PageRequest.of(page, size, s);
        Page<ProcessListDto> processes = processService.getProcessList(pageable);
        return ResponseEntity.ok(processes);
    }

    @PreAuthorize("@permChecker.has(authentication, '/process','write') or hasAnyRole('ADMIN','OP')")
    @PostMapping
    public ResponseEntity<ProcessDetailDto> createProcess(@Valid @RequestBody ProcessCreationDto creationDto) {
        ProcessDetailDto newProcess = processService.createProcess(creationDto);
        return new ResponseEntity<>(newProcess, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProcessDetail(@PathVariable("id") String processId, HttpServletRequest req) {
        try {
            ProcessDetailDto detail = processService.getProcessDetail(processId);

            if (detail == null) {
                return ResponseEntity.status(404).body(Map.of(
                        "code", "ID_NOT_FOUND",
                        "message", "해당 공정 ID를 찾을 수 없습니다",
                        "path", req.getRequestURI(),
                        "method", req.getMethod()
                ));
            }
            return ResponseEntity.ok(detail);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "code", "INTERNAL_ERROR",
                    "message", e.getMessage(),
                    "path", req.getRequestURI(),
                    "method", req.getMethod()
            ));
        }
    }

    @PreAuthorize("@permChecker.has(authentication, '/process','write') or hasAnyRole('ADMIN','OP')")
    @PutMapping("/{id}")
    public ResponseEntity<ProcessDetailDto> updateProcess(
            @PathVariable("id") String processId,
            @Valid @RequestBody ProcessCreationDto creationDto) {
        try {
            ProcessDetailDto updatedProcess = processService.updateProcess(processId, creationDto);
            return ResponseEntity.ok(updatedProcess);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
}