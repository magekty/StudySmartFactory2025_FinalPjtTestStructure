// src/main/java/com/demo/erp/security/ApiKeyAuthFilter.java
package com.demo.erp.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;

public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private final String apiKey;

    public ApiKeyAuthFilter(String apiKey) { this.apiKey = apiKey; }

    private static final Set<String> EXACT_ALLOW = Set.of(
            "/", "/swagger-ui.html", "/actuator/health", "/error", "/favicon.ico"
    );
    private static final List<String> PREFIX_ALLOW = List.of(
            "/swagger-ui/", "/v3/api-docs/", "/actuator/"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (apiKey == null || apiKey.isBlank()) return true;
        String uri = request.getRequestURI();
        String ctx = request.getContextPath();
        String path = (ctx != null && !ctx.isEmpty() && uri.startsWith(ctx)) ? uri.substring(ctx.length()) : uri;
        if (EXACT_ALLOW.contains(path)) return true;
        for (String p : PREFIX_ALLOW) if (path.startsWith(p)) return true;
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
      """.formatted(req.getRequestURI(), req.getMethod(), OffsetDateTime.now(ZoneOffset.UTC).toString());
        res.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));
    }
}