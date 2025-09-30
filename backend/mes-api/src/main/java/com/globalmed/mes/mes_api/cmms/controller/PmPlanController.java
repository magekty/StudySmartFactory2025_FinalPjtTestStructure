package com.globalmed.mes.mes_api.cmms.controller;

import com.globalmed.mes.mes_api.cmms.dto.*;
import com.globalmed.mes.mes_api.cmms.service.PmPlanService;
import com.globalmed.mes.mes_api.common.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("/cmms/pm-plans")
@RequiredArgsConstructor
public class PmPlanController {
    private final PmPlanService service;

    @PreAuthorize("@permChecker.has(authentication, '/cmms/pm-plans','write') or hasAnyRole('ADMIN','OP')")
    @PostMapping
    public PmPlanDto.Res create(@RequestBody @Valid PmPlanDto.CreateReq req,
                                Authentication auth) {
        String actor = principalToUserId(auth);
        return service.create(req, actor);
    }

    @PreAuthorize("@permChecker.has(authentication, '/cmms/pm-plans','write') or hasAnyRole('ADMIN','OP')")
    @PostMapping("/{id}/done")
    public PmPlanDto.Res markDone(@PathVariable Long id,
                                  @RequestParam OffsetDateTime doneAt,
                                  Authentication auth) {
        String actor = principalToUserId(auth);
        return service.markDoneAndRoll(id, doneAt, actor);
    }

    @GetMapping("/due")
    public PageResponse<PmPlanDto.Res> due(@RequestParam(required = false) OffsetDateTime to,
                                           @RequestParam(required = false) String equipmentId,
                                           @RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "10") int size,
                                           @RequestParam(defaultValue = "nextDueAt,asc") String sort) {
        String[] s = sort.split(",", 2);
        String prop = s[0].trim();
        Sort.Direction dir = (s.length > 1 && "desc".equalsIgnoreCase(s[1].trim()))
                ? Sort.Direction.DESC : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(new Sort.Order(dir, prop)));
        var p = service.findDue(to, equipmentId, pageable);
        return PageResponse.of(p, pageable.getSort().toString());
    }
    private String principalToUserId(Authentication auth) {
        return (auth == null) ? null : String.valueOf(auth.getPrincipal());
    }
}
