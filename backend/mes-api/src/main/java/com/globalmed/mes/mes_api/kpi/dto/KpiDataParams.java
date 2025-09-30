package com.globalmed.mes.mes_api.kpi.dto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Data
@Accessors(chain = true) // 체이닝 가능
public class KpiDataParams {
    private BigDecimal goodQty;
    private BigDecimal defectQty;
    private BigDecimal totalQty;
    private BigDecimal runTime;
    private BigDecimal plannedTime;
    private BigDecimal producedQty;
    private BigDecimal hundred = BigDecimal.valueOf(100);

    private static final ObjectMapper mapper = new ObjectMapper();

    public Map<String, Object> toMap() {
        Map<String, Object> map = mapper.convertValue(this, new TypeReference<Map<String, Object>>() {});
        map.entrySet().removeIf(e -> e.getValue() == null); // null 제거
        Map<String, Object> snakeMap = new HashMap<>();
        map.forEach((k, v) -> snakeMap.put(k.replaceAll("([A-Z])", "_$1").toLowerCase(), v));
        snakeMap.put("hundred", this.getHundred()); // 항상 포함
        return snakeMap;
    }

}
