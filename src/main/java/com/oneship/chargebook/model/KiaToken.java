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
    private LocalDateTime expirationTime;
    private String code;  // 인증 코드 추가

    public KiaToken(String userId, String accessToken, String refreshToken, String tokenType, int expiresIn, String code) {
        this.userId = userId;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
        this.expirationTime = LocalDateTime.now().plusSeconds(expiresIn);
        this.code = code;
    }

    public boolean isTokenExpired() {
        return LocalDateTime.now().isAfter(expirationTime);
    }
}
