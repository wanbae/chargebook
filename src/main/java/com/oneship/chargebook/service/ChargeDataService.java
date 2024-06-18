package com.oneship.chargebook.service;

import com.oneship.chargebook.model.ChargeData;
import com.oneship.chargebook.repository.ChargeDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ChargeDataService {

    @Autowired
    private ChargeDataRepository chargeDataRepository;

    public List<ChargeData> getChargeDataByMonth(String month) {
        return chargeDataRepository.findByMonth(month);
    }

    public List<ChargeData> getAllChargeData() {
        return chargeDataRepository.findAll();
    }

    public Optional<ChargeData> getChargeDataById(Long id) {
        return chargeDataRepository.findById(id);
    }

    public void saveChargeData(ChargeData chargeData) {
        calculateValues(chargeData);
        chargeDataRepository.save(chargeData);
    }

    public void deleteChargeData(Long id) {
        chargeDataRepository.deleteById(id);
    }

    public int getAccumulatedDistance() {
        return chargeDataRepository.getAccumulatedDistance();
    }

    public List<Object[]> getTotalChargeByCompany(String month) {
        return chargeDataRepository.findTotalChargeByCompany(month);
    }

    public List<Object[]> getTotalPriceByCard(String month) {
        return chargeDataRepository.findTotalPriceByCard(month);
    }

    private void calculateValues(ChargeData chargeData) {
        int price = chargeData.getPrice() != null ? chargeData.getPrice() : 0;
        int point = chargeData.getPoint() != null ? chargeData.getPoint() : 0;
        int discountRate = chargeData.getDiscountRate() != null ? chargeData.getDiscountRate() : 0;
        int amountOfCharge = chargeData.getAmountOfCharge() != null ? chargeData.getAmountOfCharge() : 0;

        int discountedPrice = price - point;
        int finalPrice = Math.round(discountedPrice * (100 - discountRate) / 100);
        int finalUnitPrice = amountOfCharge == 0 ? 0 : Math.round(finalPrice / amountOfCharge);

        chargeData.setDiscountedPrice(discountedPrice);
        chargeData.setFinalPrice(finalPrice);
        chargeData.setFinalUnitPrice(finalUnitPrice);
    }
}
