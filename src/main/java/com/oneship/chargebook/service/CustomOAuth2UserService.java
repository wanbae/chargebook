package com.oneship.chargebook.service;

import com.oneship.chargebook.model.User;
import com.oneship.chargebook.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oauth2User = super.loadUser(userRequest);

        String email = oauth2User.getAttribute("email");
        User user = userRepository.findByUsername(email);
        if (user == null) {
            user = new User();
            user.setUsername(email);
            user.setRole("USER");
            userRepository.save(user);
        }

        Map<String, Object> attributes = oauth2User.getAttributes();
        return new DefaultOAuth2User(Collections.singleton(new OAuth2UserAuthority(attributes)), attributes, "email");
    }
}
