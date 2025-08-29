// src/main/java/com/demo/erp/web/ApiKeyFilter.java
package com.demo.erp.web;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.Map;

@Component
public class ApiKeyFilter implements Filter {
    @Value("${erp.apiKey:}")
    private String apiKey;
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest http = (HttpServletRequest) req;
        HttpServletResponse resp = (HttpServletResponse) res;
        if (apiKey != null && !apiKey.isBlank()) {
            String got = http.getHeader("X-API-Key");
            if (got == null || !got.equals(apiKey)) {
                resp.setStatus(401);
                resp.setContentType("application/json");
                resp.getWriter().write("""
          {"code":"UNAUTHORIZED","message":"UNAUTHORIZED","path":"%s","method":"%s"}""".formatted(http.getRequestURI(), http.getMethod()));
                return;
            }
        }
        chain.doFilter(req, res);
    }
}