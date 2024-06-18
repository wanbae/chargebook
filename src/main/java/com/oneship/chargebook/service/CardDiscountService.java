package com.oneship.chargebook.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.oneship.chargebook.config.CardDiscountConfig;

@Service
public class CardDiscountService {

    @Autowired
    private CardDiscountConfig cardDiscountConfig;

    public int getDiscountByCardName(String cardName) {
        Map<String, Integer> discounts = cardDiscountConfig.getDiscounts();
        return discounts.getOrDefault(cardName, 0);
    }
}
