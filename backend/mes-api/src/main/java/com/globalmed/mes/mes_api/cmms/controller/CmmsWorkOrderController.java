package com.globalmed.mes.mes_api.cmms.controller;

import com.globalmed.mes.mes_api.cmms.dto.*;
import com.globalmed.mes.mes_api.cmms.service.CmmsWorkOrderService;
import com.globalmed.mes.mes_api.common.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/cmms/work-orders")
@RequiredArgsConstructor
public class CmmsWorkOrderController {
    private final CmmsWorkOrderService service;

    @PreAuthorize("@permChecker.has(authentication, '/cmms/work-orders','write') or hasAnyRole('ADMIN','OP')")
    @PostMapping
    public CmmsWorkOrderDto.Res create(@RequestBody @Valid CmmsWorkOrderDto.createReq req,
                                       Authentication auth){
        String actorUserId = principalToUserId(auth);
        return service.create(req, actorUserId);
    }

    @PreAuthorize("@permChecker.has(authentication, '/cmms/work-orders','write') or hasAnyRole('ADMIN','OP')")
    @PostMapping("/{id}/assign")
    public CmmsWorkOrderDto.Res assign(@PathVariable Long id,
                                       @RequestBody @Valid CmmsWorkOrderDto.AssignReq req,
                                       Authentication auth){
        String actorUserId = principalToUserId(auth);
        return service.assign(id, req, actorUserId);
    }

    @PreAuthorize("@permChecker.has(authentication, '/cmms/work-orders','write') or hasAnyRole('ADMIN','OP')")
    @PostMapping("/{id}/start")
    public CmmsWorkOrderDto.Res start(@PathVariable Long id,
                                      @RequestBody CmmsWorkOrderDto.startReq req,
                                      Authentication auth) {
        String actorUserId = principalToUserId(auth);
        return service.start(id, req, actorUserId);
    }

    @PreAuthorize("@permChecker.has(authentication, '/cmms/work-orders','write') or hasAnyRole('ADMIN','OP')")
    @PostMapping("/{id}/complete")
    public CmmsWorkOrderDto.Res complete(@PathVariable Long id,
                                         @RequestBody CmmsWorkOrderDto.finishReq req,
                                         Authentication auth) {
        String actorUserId = principalToUserId(auth);
        return service.complete(id, req, actorUserId);
    }

    @GetMapping
    public PageResponse<CmmsWorkOrderDto.Res> search(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String equipmentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort
    ) {
        String[] s = sort.split(",", 2);
        String prop = s[0].trim();
        Sort.Direction dir = (s.length > 1 && "desc".equalsIgnoreCase(s[1].trim()))
                ? Sort.Direction.DESC : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(new Sort.Order(dir, prop)));
        var p = service.search(status, equipmentId, pageable);
        return PageResponse.of(p, pageable.getSort().toString());
    }

    private String principalToUserId(Authentication auth) {
        return (auth == null) ? null : String.valueOf(auth.getPrincipal());
    }
}
