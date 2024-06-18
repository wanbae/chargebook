package com.oneship.chargebook.config;

import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "card-discounts")
public class CardDiscountConfig {

    private static final Logger logger = LoggerFactory.getLogger(CardDiscountConfig.class);

    private Map<String, Integer> discounts;

    public Map<String, Integer> getDiscounts() {
        return discounts;
    }

    public void setDiscounts(Map<String, Integer> discounts) {
        this.discounts = discounts;
    }

    @PostConstruct
    public void postConstruct() {
        logger.info("Loaded card discounts: {}", discounts);
    }
}
