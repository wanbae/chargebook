package com.oneship.chargebook.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_tokens")
public class KiaToken {
    @Id
    private String userId;
    @Column(length = 1024)
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private int expiresIn;
    private LocalDateTime accessTokenExpirationTime;
    private LocalDateTime refreshTokenExpirationTime;  // refreshToken의 만료 시간 추가
    private String code;  // 인증 코드 추가

    public KiaToken(String userId, String accessToken, String refreshToken, String tokenType, int expiresIn, String code) {
        this.userId = userId;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
        this.accessTokenExpirationTime = LocalDateTime.now().plusSeconds(expiresIn);
        this.refreshTokenExpirationTime = LocalDateTime.now().plusYears(1);  // refreshToken 만료 시간 1년으로 설정
        this.code = code;
    }

    public boolean isAccessTokenExpired() {
        return accessTokenExpirationTime==null || LocalDateTime.now().isAfter(accessTokenExpirationTime);
    }

    public boolean isRefreshTokenExpired() {
        return refreshTokenExpirationTime == null || LocalDateTime.now().isAfter(refreshTokenExpirationTime);
    }
}
