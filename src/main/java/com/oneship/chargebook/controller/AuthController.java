package com.oneship.chargebook.controller;

import com.oneship.chargebook.config.KiaProperties;
import com.oneship.chargebook.model.KiaToken;
import com.oneship.chargebook.service.KiaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    @Autowired
    private KiaService kiaService;

    @Autowired
    private KiaProperties kiaProperties;  // KiaProperties 빈 주입

    @GetMapping("/kia/sso")
    public String login() {
        String clientId = kiaProperties.getClientId();  // KiaProperties에서 가져옴
        String redirectUri = kiaProperties.getRedirectUri();  // KiaProperties에서 가져옴
        String state = kiaProperties.getState();

        String authUrl = "https://prd.kr-ccapi.kia.com/api/v1/user/oauth2/authorize" +
                "?response_type=code" +
                "&client_id=" + clientId +
                "&redirect_uri=" + redirectUri +
                "&state=" + state;

        return "redirect:" + authUrl;
    }

    @GetMapping("/kia/callback")
    public String callback(@RequestParam("code") String code, @RequestParam("state") String state, Authentication authentication) {
        // state 검증
        if (!validateState(state)) {
            return "error";
        }

        // 인증 코드로 토큰 발급 요청
        String userId = authentication.getName();
        KiaToken kiaToken = kiaService.requestKiaToken(userId, code);

        if (kiaToken == null) {
            return "error";
        }

        return "redirect:/";
    }

    private boolean validateState(String state) {
        // state 토큰 검증 로직 구현
        return state != null && state.equals(kiaProperties.getState());
    }
}
