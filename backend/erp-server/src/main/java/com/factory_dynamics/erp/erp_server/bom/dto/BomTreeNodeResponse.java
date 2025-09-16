// src/main/java/com/factory_dynamics/erp/erp_server/bom/dto/BomTreeNodeResponse.java
package com.factory_dynamics.erp.erp_server.bom.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class BomTreeNodeResponse {
    public String bomLineId;
    public String parentLineId;
    public String componentProductId;
    public String componentCode;
    public String componentName;
    public BigDecimal qty;
    public BigDecimal scrapRate;
    public String note;
    public List<BomTreeNodeResponse> children = new ArrayList<>();
}