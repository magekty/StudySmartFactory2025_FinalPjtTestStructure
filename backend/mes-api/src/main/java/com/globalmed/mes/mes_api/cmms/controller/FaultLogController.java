package com.globalmed.mes.mes_api.cmms.controller;

import com.globalmed.mes.mes_api.cmms.dto.*;
import com.globalmed.mes.mes_api.cmms.service.FaultLogService;
import com.globalmed.mes.mes_api.common.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;


@RestController
@RequestMapping("/cmms/fault-logs")
@RequiredArgsConstructor
public class FaultLogController {
    private final FaultLogService service;

    @PreAuthorize("@permChecker.has(authentication, '/fault-logs','write') or hasAnyRole('ADMIN','OP')")
    @PostMapping
    public FaultLogDto.Res create(@RequestBody @Valid FaultLogDto.CreateReq req,
                                  Authentication auth)
    {
        String actor = (auth == null) ? null : String.valueOf(auth.getPrincipal());
        return service.create(req, actor);
    }

    @GetMapping
    public PageResponse<FaultLogDto.Res> search(@RequestParam(required = false) String equipmentId,
                                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
                                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
                                                @RequestParam(defaultValue ="0") int page,
                                                @RequestParam(defaultValue ="10") int size,
                                                @RequestParam(defaultValue = "occurredAt,desc") String sort){
        String[] p = sort.split(",", 2);
        Sort.Direction dir = (p.length>1 && p[1].equalsIgnoreCase("desc")) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort s = Sort.by(new Sort.Order(dir, p[0]));
        Pageable pageable = PageRequest.of(page, size, s);

        var result = service.search(equipmentId, from, to, pageable);
        return PageResponse.of(result, s.toString());
    }
}
