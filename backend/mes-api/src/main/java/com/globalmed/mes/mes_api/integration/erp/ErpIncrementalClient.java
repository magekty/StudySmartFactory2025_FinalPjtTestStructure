package com.globalmed.mes.mes_api.integration.erp;

import com.globalmed.mes.mes_api.integration.erp.dto.*;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.OffsetDateTime;
import java.util.List;

@Component
public class ErpIncrementalClient {
    private final RestClient rc;
    public ErpIncrementalClient(RestClient erpRestClient){ this.rc = erpRestClient; }

    public List<ItemDto> items(OffsetDateTime updatedSince) {
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
}