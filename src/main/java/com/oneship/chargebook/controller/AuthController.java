package com.oneship.chargebook.controller;

import com.oneship.chargebook.config.KiaProperties;
import com.oneship.chargebook.model.KiaToken;
import com.oneship.chargebook.model.User;
import com.oneship.chargebook.service.KiaService;
import com.oneship.chargebook.service.UserService;
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
    private UserService userService;

    @Autowired
    private KiaProperties kiaProperties;  // KiaProperties 필드 추가
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
            // state 값이 유효하지 않으면 에러 페이지로 리디렉트
            return "error";
        }

        // 인증 코드로 토큰 발급 요청
        String username = authentication.getName(); // 현재 인증된 사용자의 이름 가져오기
        User user = userService.findByUsername(username); // 사용자 정보 조회
        KiaToken kiaToken = kiaService.requestKiaToken(user.getId(), code); // KiaToken 요청

        if (kiaToken == null) {
            // KiaToken 발급 실패 시 에러 페이지로 리디렉트
            return "error";
        }

        // 정상적으로 토큰이 발급되면 홈 페이지로 리디렉트
        return "redirect:/";
    }

    private boolean validateState(String state) {
        return state != null && state.equals(kiaProperties.getState());
    }
}
