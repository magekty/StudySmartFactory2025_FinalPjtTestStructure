package com.globalmed.mes.mes_api.auth.controller;

import com.globalmed.mes.mes_api.auth.service.CaptchaService;
import com.wf.captcha.SpecCaptcha;
import com.wf.captcha.base.Captcha;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class CaptchaController {

    private final CaptchaService captchaService;

    @GetMapping("/captcha")
    public void getCaptcha(HttpServletResponse response, HttpSession session) throws IOException {
        // ✅ SpecCaptcha 생성 (너비 130, 높이 48, 자리수 4)
        SpecCaptcha captcha = new SpecCaptcha(130, 48, 4);
        captcha.setCharType(Captcha.TYPE_DEFAULT);

        // ✅ 정답 세션에 저장
        captchaService.storeCaptcha(session, captcha.text().toLowerCase());

        // ✅ 응답 헤더 설정
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setContentType("image/png");

        // ✅ 이미지 출력
        captcha.out(response.getOutputStream());
    }
}