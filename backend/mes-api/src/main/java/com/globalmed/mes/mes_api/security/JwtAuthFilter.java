// security/JwtAuthFilter.java
package com.globalmed.mes.mes_api.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
public class JwtAuthFilter extends GenericFilter {
    @Value("${app.security.jwt.secret}") String secret;

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        var http = (HttpServletRequest) req;
        var authz = http.getHeader("Authorization");
        if (authz != null && authz.startsWith("Bearer ")) {
            var token = authz.substring(7);
            try {
                var key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
                var claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
                var userId = claims.getSubject();
                @SuppressWarnings("unchecked")
                var roles = (List<String>) claims.get("roles");
                var auth = new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (Exception ignore) {}
        }
        chain.doFilter(req, res);
    }
}