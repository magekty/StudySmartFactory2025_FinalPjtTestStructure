package com.globalmed.mes.mes_api.common.api;

import lombok.Builder;
import lombok.Getter;
import java.util.Map;

// common/api/ErrorResponse.java
@Getter
@Builder
public class ErrorResponse {
    private String code; private String message; private Map<String,Object> details;
    private String traceId; private String timestamp; private String path; private String method;
}

