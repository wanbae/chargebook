package com.oneship.chargebook.service;

import com.oneship.chargebook.model.KiaToken;
import com.oneship.chargebook.model.User;
import com.oneship.chargebook.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.oneship.chargebook.repository.KiaTokenRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private KiaTokenRepository kiaTokenRepository;

    public User saveUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getRole() == null) {
            user.setRole("USER"); // 기본 역할 설정
        }
        return userRepository.save(user);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public KiaToken getKiaTokenByUserId(String userId) {
        return kiaTokenRepository.findById(userId).orElse(null);
    }
}
