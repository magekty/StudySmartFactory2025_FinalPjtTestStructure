// rbac/MenuController.java
package com.globalmed.mes.mes_api.rbac.controller;

import com.globalmed.mes.mes_api.rbac.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @GetMapping("/my")
    public ResponseEntity<?> myMenus() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        // In JwtAuthFilter, subject is set as userId
        String userId = String.valueOf(auth.getPrincipal());

        return ResponseEntity.ok(Map.of(
                "user", userId,
                "menus", menuService.getTreeForUser(userId)
        ));
    }
}