// src/main/java/com/globalmed/mes/mes_api/integration/erp/ErpIncrementalClient.java
package com.globalmed.mes.mes_api.integration.erp;

import com.globalmed.mes.mes_api.integration.erp.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ErpIncrementalClient {
    private final RestClient rc;

    public List<ItemDto> items(OffsetDateTime updatedSince) {
        String qs = (updatedSince == null) ? "" : "?updatedSince=" + URLEncoder.encode(updatedSince.toString(), StandardCharsets.UTF_8);
        log.info("[MES->ERP] GET /items{}", qs);

        ResponseEntity<List<ItemDto>> res = rc.get()
                .uri(uri -> {
                    var b = uri.path("/items");
                    if (updatedSince != null) b.queryParam("updatedSince", updatedSince.toString());
                    return b.build();
                })
                .retrieve()
                .toEntity(new ParameterizedTypeReference<List<ItemDto>>() {});
        return res.getBody();
    }

    public List<BomHeaderDto> boms(OffsetDateTime updatedSince) {
        String qs = (updatedSince == null) ? "" : "?updatedSince=" + URLEncoder.encode(updatedSince.toString(), StandardCharsets.UTF_8);
        log.info("[MES->ERP] GET /boms{}", qs);

        ResponseEntity<List<BomHeaderDto>> res = rc.get()
                .uri(uri -> {
                    var b = uri.path("/boms");
                    if (updatedSince != null) b.queryParam("updatedSince", updatedSince.toString());
                    return b.build();
                })
                .retrieve()
                .toEntity(new ParameterizedTypeReference<List<BomHeaderDto>>() {});
        return res.getBody();
    }

    public List<PlanLineDto> plans(OffsetDateTime updatedSince, int page, int size) {
        String qs = "?page=" + page + "&size=" + size
                + (updatedSince == null ? "" : "&updatedSince=" + URLEncoder.encode(updatedSince.toString(), StandardCharsets.UTF_8));
        log.info("[MES->ERP] GET /plans{}", qs);

        ResponseEntity<List<PlanLineDto>> res = rc.get()
                .uri(uri -> {
                    var b = uri.path("/plans").queryParam("page", page).queryParam("size", size);
                    if (updatedSince != null) b.queryParam("updatedSince", updatedSince.toString());
                    return b.build();
                })
                .retrieve()
                .toEntity(new ParameterizedTypeReference<List<PlanLineDto>>() {});
        return res.getBody();
    }
}