package com.oneship.chargebook;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.oneship.chargebook.config.CardDiscountConfig;

@SpringBootApplication
@EnableConfigurationProperties(CardDiscountConfig.class)
public class ChargebookApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChargebookApplication.class, args);
    }
}
