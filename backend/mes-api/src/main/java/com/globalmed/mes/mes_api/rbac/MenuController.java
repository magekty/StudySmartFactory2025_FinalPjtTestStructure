// rbac/MenuController.java
package com.globalmed.mes.mes_api.rbac;


import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController @RequestMapping("/menus")
@RequiredArgsConstructor
public class MenuController {
    private final MenuService svc;
    @GetMapping("/my")
    public Map<String,Object> my(){
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        String userId = String.valueOf(auth.getPrincipal()); // 토큰 subject = userId
        return Map.of("user", userId, "menus", svc.getMenusForUser(userId));
    }
}