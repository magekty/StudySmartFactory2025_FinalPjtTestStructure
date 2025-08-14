// auth/AuthController.java
package com.globalmed.mes.mes_api.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController @RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginReq req){
        var res = userService.authenticate(req.username(), req.password());
        return ResponseEntity.ok(Map.of(
                "token", res.token(),
                "user", Map.of("userId", res.userId(), "roles", res.roles())
        ));
    }
    public record LoginReq(@NotBlank String username, @NotBlank String password){}
}