// auth/JwtUtil.java
package com.globalmed.mes.mes_api.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

@Component
public class JwtUtil {
    private final String secret;
    private final long ttlMinutes;

    public JwtUtil(
            @Value("${app.security.jwt.secret}") String secret,
            @Value("${app.security.jwt.ttl-minutes}") long ttlMinutes
    ){
        this.secret = secret; this.ttlMinutes = ttlMinutes;
    }

    public String issue(String userId, List<String> roles){
        var key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        var now = Instant.now();
        var exp = now.plusSeconds(ttlMinutes * 60);
        return Jwts.builder()
                .subject(userId)
                .claim("roles", roles)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key)
                .compact();
    }
}