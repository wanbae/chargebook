package com.oneship.chargebook.service;

import com.oneship.chargebook.model.ChargeData;
import com.oneship.chargebook.model.User;
import com.oneship.chargebook.repository.ChargeDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ChargeDataService {

    private static final Logger logger = LoggerFactory.getLogger(ChargeDataService.class);

    @Autowired
    private ChargeDataRepository chargeDataRepository;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private KiaService kiaService;

    public List<ChargeData> getChargeDataByMonthAndUser(String month, User user) {
        return chargeDataRepository.findByMonthAndUser(month, user);
    }

    public List<ChargeData> getAllChargeDataByUser(User user) {
        return chargeDataRepository.findByUser(user);
    }

    public Optional<ChargeData> getChargeDataByIdAndUser(Long id, User user) {
        return chargeDataRepository.findByIdAndUser(id, user);
    }

    public void saveChargeData(ChargeData chargeData, Principal principal) {
        User user = userDetailsService.getUserByPrincipal(principal);
        chargeData.setUser(user);

        ensureNonNullValues(chargeData);
        calculateValues(chargeData);
        chargeDataRepository.save(chargeData);
    }

    public void updateChargeData(ChargeData chargeData, Principal principal) {
        User user = userDetailsService.getUserByPrincipal(principal);
        chargeData.setUser(user);

        ensureNonNullValues(chargeData);
        calculateValues(chargeData);
        chargeDataRepository.save(chargeData);
    }

    public void deleteChargeData(Long id, User user) {
        Optional<ChargeData> chargeData = chargeDataRepository.findByIdAndUser(id, user);
        chargeData.ifPresent(chargeDataRepository::delete);
    }

    public long getAccumulatedDistance(User user) {
        try {
            return kiaService.getOdometer(user.getId());
        } catch (Exception e) {
            logger.error("Error getting accumulated distance", e);
            return 0;
        }
    }

    public Double getBatteryStatus(User user) {
        try {
            return kiaService.getBatteryStatus(user.getId());
        } catch (Exception e) {
            logger.error("Error getting battery status", e);
            return 0.0;
        }
    }

    public Double getDrivingRange(User user) {
        try {
            return kiaService.getDte(user.getId());
        } catch (Exception e) {
            logger.error("Error getting driving range", e);
            return 0.0;
        }
    }

    public Map<String, Integer> getTotalPriceByCard(String month, User user) {
        List<ChargeData> monthlyData = getChargeDataByMonthAndUser(month, user);
        Map<String, Integer> totalPriceByCard = new HashMap<>();
        for (ChargeData data : monthlyData) {
            totalPriceByCard.merge(data.getCard(), data.getPrice(), Integer::sum);
        }
        return totalPriceByCard;
    }

    public Map<String, Integer> getTotalChargeByCompany(String month, User user) {
        List<ChargeData> monthlyData = getChargeDataByMonthAndUser(month, user);
        Map<String, Integer> totalChargeByCompany = new HashMap<>();
        for (ChargeData data : monthlyData) {
            totalChargeByCompany.merge(data.getCompany(), data.getAmountOfCharge().intValue(), Integer::sum);
        }
        return totalChargeByCompany;
    }

    private void ensureNonNullValues(ChargeData chargeData) {
        if (chargeData.getAmountOfCharge() == null) {
            chargeData.setAmountOfCharge(0.0);
        }
        if (chargeData.getPrice() == null) {
            chargeData.setPrice(0);
        }
        if (chargeData.getPoint() == null) {
            chargeData.setPoint(0);
        }
        if (chargeData.getDistance() == null) {
            chargeData.setDistance(0);
        }
        if (chargeData.getDiscountRate() == null) {
            chargeData.setDiscountRate(0);
        }
        if (chargeData.getDiscountedPrice() == null) {
            chargeData.setDiscountedPrice(0);
        }
        if (chargeData.getFinalPrice() == null) {
            chargeData.setFinalPrice(0);
        }
        if (chargeData.getFinalUnitPrice() == null) {
            chargeData.setFinalUnitPrice(0);
        }
        if (chargeData.getBatteryStatus() == null) {
            chargeData.setBatteryStatus(0.0);
        }
        if (chargeData.getDrivingRange() == null) {
            chargeData.setDrivingRange(0.0);
        }
    }

    private void calculateValues(ChargeData chargeData) {
        int price = chargeData.getPrice() != null ? chargeData.getPrice() : 0;
        int point = chargeData.getPoint() != null ? chargeData.getPoint() : 0;
        int discountRate = chargeData.getDiscountRate() != null ? chargeData.getDiscountRate() : 0;
        double amountOfCharge = chargeData.getAmountOfCharge() != null ? chargeData.getAmountOfCharge() : 0.0;

        int discountedPrice = price - point;
        int finalPrice = Math.round(discountedPrice * (100 - discountRate) / 100);
        int finalUnitPrice = amountOfCharge == 0 ? 0 : Math.round(finalPrice / (float) amountOfCharge);

        chargeData.setDiscountedPrice(discountedPrice);
        chargeData.setFinalPrice(finalPrice);
        chargeData.setFinalUnitPrice(finalUnitPrice);
    }
}
