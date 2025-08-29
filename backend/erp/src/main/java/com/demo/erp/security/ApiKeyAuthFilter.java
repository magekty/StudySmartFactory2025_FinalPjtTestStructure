// src/main/java/com/demo/erp/security/ApiKeyAuthFilter.java
package com.demo.erp.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    @Value("${erp.apiKey:}")
    private String apiKey;

    private static final Set<String> EXACT_ALLOW = Set.of(
            "/", "/swagger-ui.html", "/actuator/health", "/error", "/favicon.ico"
    );
    private static final List<String> PREFIX_ALLOW = List.of(
            "/swagger-ui/", "/v3/api-docs/"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (apiKey == null || apiKey.isBlank()) return true;
        String path = request.getServletPath();
        if (EXACT_ALLOW.contains(path)) return true;
        for (String prefix : PREFIX_ALLOW) {
            if (path.startsWith(prefix)) return true;
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String got = request.getHeader("X-API-Key");
        if (got == null || !got.equals(apiKey)) {
            writeUnauthorizedJson(response, request);
            return;
        }

        var auth = new UsernamePasswordAuthenticationToken(
                "api-key-user",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_API"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        filterChain.doFilter(request, response);
    }

    private void writeUnauthorizedJson(HttpServletResponse res, HttpServletRequest req) throws IOException {
        res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        res.setContentType("application/json;charset=UTF-8");
        String body = """
      {"code":"UNAUTHORIZED","message":"UNAUTHORIZED","path":"%s","method":"%s","timestamp":"%s"}
      """.formatted(req.getRequestURI(), req.getMethod(), OffsetDateTime.now().toString());
        res.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));
    }
}