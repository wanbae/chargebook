package com.oneship.chargebook.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KiaToken {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private int expiresIn;
    private LocalDateTime expirationTime;

    public KiaToken(String accessToken, String refreshToken, String tokenType, int expiresIn) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
        this.expirationTime = LocalDateTime.now().plusSeconds(expiresIn);
    }

    public boolean isTokenExpired() {
        return LocalDateTime.now().isAfter(expirationTime);
    }
}
