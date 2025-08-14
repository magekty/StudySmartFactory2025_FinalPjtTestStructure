package com.globalmed.mes.mes_api.auth;

import com.globalmed.mes.mes_api.rbac.UserRoleRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service @RequiredArgsConstructor
public class UserService {
    private final UserRepo userRepo;
    private final UserRoleRepo userRoleRepo; // ← 추가
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);

    public AuthResult authenticate(String username, String rawPassword){
        var user = userRepo.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("AUTH_REQUIRED"));
        if (user.getIsActive() != 1) throw new IllegalStateException("FORBIDDEN");
        if (!encoder.matches(rawPassword, user.getPasswordHash()))
            throw new IllegalArgumentException("AUTH_REQUIRED");

        var roles = userRoleRepo.findRoleCodes(user.getUserId()); // ← 여기로 변경
        var token = jwtUtil.issue(user.getUserId(), roles);
        return new AuthResult(user.getUserId(), roles, token);
    }
    public record AuthResult(String userId, List<String> roles, String token){}
}