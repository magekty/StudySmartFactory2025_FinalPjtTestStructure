// src/main/java/com/globalmed/mes/mes_api/security/PermChecker.java
package com.globalmed.mes.mes_api.security;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("permChecker")
@RequiredArgsConstructor
public class PermChecker {
    private final JdbcTemplate jdbc;

    public boolean has(Authentication auth, String path, String action) {
        try {
            if (auth == null || !auth.isAuthenticated()) return false;
            if (path == null || path.isBlank()) return false;

            final String userId = String.valueOf(auth.getPrincipal());
            final String col = switch (action == null ? "" : action) {
                case "write" -> "rm.allow_write";
                case "exec"  -> "rm.allow_exec";
                default      -> "rm.allow_read";
            };

            final String sql = """
        select coalesce(max(%s),0)
          from tb_user_role ur
          join tb_role_menu rm on rm.role_id = ur.role_id
          join tb_menu m on m.menu_id = rm.menu_id
         where ur.user_id = ?
           and (m.path = ? or ? like concat(m.path, '/%%'))
      """.formatted(col);

            Integer allow = jdbc.queryForObject(sql, Integer.class, userId, path, path);
            return allow != null && allow == 1;
        } catch (Exception ignore) {
            // 어떤 예외도 바깥으로 던지지 말 것: SpEL 실패 방지
            return false;
        }
    }
}