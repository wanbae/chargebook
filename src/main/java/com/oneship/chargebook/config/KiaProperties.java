package com.oneship.chargebook.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "kia")
public class KiaProperties {
    private String clientId;
    private String clientSecret;
    private String redirectUri;
    private String refreshToken;
    private String carId;
    private String state;
    private Api api;

    @Data
    public static class Api {
        private String baseUrl;
        private String tokenUrl;
    }
}
