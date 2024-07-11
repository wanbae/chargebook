package com.oneship.chargebook.controller;

import com.oneship.chargebook.model.KiaToken;
import com.oneship.chargebook.model.User;
import com.oneship.chargebook.service.KiaService;
import com.oneship.chargebook.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private KiaService kiaService;

    @GetMapping("/profile")
    public String profile(Model model, Authentication authentication) {
        String username = authentication.getName();
        User user = userService.findByUsername(username);
        KiaToken kiaToken = kiaService.getKiaTokenByUserId(user.getId());

        model.addAttribute("user", user);
        if (kiaToken != null) {
            model.addAttribute("refreshTokenExpirationTime", kiaToken.getRefreshTokenExpirationTime());
        } else {
            model.addAttribute("refreshTokenExpirationTime", null);
        }

        return "profile";
    }

    // @GetMapping("/register")
    // public String register(Model model) {
    //     model.addAttribute("user", new User());
    //     return "register";
    // }

    // @PostMapping("/register")
    // public String registerUser(User user) {
    //     userService.saveUser(user);
    //     return "redirect:/login";
    // }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
