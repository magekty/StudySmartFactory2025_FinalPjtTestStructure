// src/main/java/com/globalmed/mes/mes_api/security/JsonAccessDeniedHandler.java
package com.globalmed.mes.mes_api.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.Map;

@Component
public class JsonAccessDeniedHandler implements AccessDeniedHandler {
    private final ObjectMapper om = new ObjectMapper();
    @Override
    public void handle(HttpServletRequest req, HttpServletResponse res, org.springframework.security.access.AccessDeniedException ex) throws IOException {
        res.setStatus(403);
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        om.writeValue(res.getOutputStream(), Map.of(
                "code","FORBIDDEN",
                "message","Access Denied",
                "path", req.getRequestURI(),
                "method", req.getMethod()
        ));
    }
}