package com.globalmed.mes.mes_api.auth.service;

import com.globalmed.mes.mes_api.auth.exception.CaptchaException;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CaptchaService {
    // cpatcha가 서버오류(500)으로 안나왔는지 인증실패(400)인지 구분하는 코드
    private static final String CAPTCHA_SESSION_KEY = "captcha";

    public void validateCaptcha(HttpSession session, String userAnswer) {
        String sessionCaptcha = (String) session.getAttribute(CAPTCHA_SESSION_KEY);

        if (sessionCaptcha == null) {
            throw new CaptchaException(
                    "CAPTCHA_NOT_FOUND",
                    "Captcha not generated.",
                    HttpStatus.INTERNAL_SERVER_ERROR, // 500
                    null
            );
        }

        if (userAnswer == null || !sessionCaptcha.equalsIgnoreCase(userAnswer.trim())) {
            throw new CaptchaException(
                    "CAPTCHA_INVALID",
                    "Invalid captcha input.",
                    HttpStatus.BAD_REQUEST, // 400
                    Map.of("input", userAnswer)
            );
        }
        clearCaptcha(session);
    }

    public void storeCaptcha(HttpSession session, String captchaValue) {
        session.setAttribute(CAPTCHA_SESSION_KEY, captchaValue);
    }

    public void clearCaptcha(HttpSession session) {
        session.removeAttribute(CAPTCHA_SESSION_KEY);
    }
}
