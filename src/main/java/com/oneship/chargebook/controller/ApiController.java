package com.oneship.chargebook.controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.oneship.chargebook.model.ChargeData;
import com.oneship.chargebook.model.User;
import com.oneship.chargebook.service.CardDiscountService;
import com.oneship.chargebook.service.ChargeDataService;
import com.oneship.chargebook.service.CustomUserDetailsService;

@RestController
public class ApiController {

    @Autowired
    private ChargeDataService chargeDataService;

    @Autowired
    private CardDiscountService cardDiscountService;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @GetMapping("/api/getDiscountRate")
    @ResponseBody
    public Map<String, Object> getDiscountRate(@RequestParam String cardName) {
        Map<String, Object> response = new HashMap<>();
        try {
            int discountRate = cardDiscountService.getDiscountByCardName(cardName);
            response.put("discountRate", discountRate);
        } catch (IllegalArgumentException e) {
            response.put("error", 1); // Error code to indicate failure
            response.put("message", e.getMessage());
        }
        return response;
    }

    @GetMapping("/api/accumulatedDistance")
    @ResponseBody
    public Map<String, Object> getAccumulatedDistance(Principal principal) {
        Map<String, Object> response = new HashMap<>();
        try {
            User user = userDetailsService.getUserByPrincipal(principal);
            long distance = chargeDataService.getAccumulatedDistance(user);
            double batteryStatus = chargeDataService.getBatteryStatus(user);
            double drivingRange = chargeDataService.getDrivingRange(user);
            response.put("distance", distance);
            response.put("batteryStatus", batteryStatus);
            response.put("drivingRange", drivingRange);
        } catch (Exception e) {
            response.put("error", 1);
            response.put("message", e.getMessage());
        }
        return response;
    }

    @GetMapping("/api/chargeHistory/{companyName}")
    @ResponseBody
    public List<ChargeData> getChargeHistoryByCompanyAndDate(
        @PathVariable String companyName, 
        @RequestParam String date,
        Principal principal
    ) {
        User user = userDetailsService.getUserByPrincipal(principal);
        return chargeDataService.getChargeDataByCompanyAndDateAndUser(companyName, date, user);
    }
}
