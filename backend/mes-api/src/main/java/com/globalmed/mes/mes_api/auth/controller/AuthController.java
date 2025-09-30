// auth/AuthController.java
package com.globalmed.mes.mes_api.auth.controller;

import com.globalmed.mes.mes_api.auth.service.CaptchaService;
import com.globalmed.mes.mes_api.auth.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final CaptchaService captchaService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginReq req, HttpSession session){
//        captchaService.validateCaptcha(session, req.captcha());
        var res = userService.authenticate(req.username(), req.password());
        return ResponseEntity.ok(Map.of(
                "token", res.token(),
                "user", Map.of("userId", res.userId(), "roles", res.roles())
        ));
    }
    public record LoginReq(@NotBlank String username, @NotBlank String password
//                           , @NotBlank String captcha
    ){}
}