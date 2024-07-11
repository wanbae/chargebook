package com.oneship.chargebook.service;

import org.springframework.web.client.RestTemplate;

import com.nimbusds.jose.shaded.json.parser.ParseException;
import com.oneship.chargebook.model.ChargeData;
import com.oneship.chargebook.model.User;
import com.oneship.chargebook.repository.ChargeDataRepository;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Optional;
import java.util.Calendar;
import java.util.Date;

import com.oneship.chargebook.service.CustomUserDetailsService;
import com.oneship.chargebook.service.KiaService;

@Service
public class ChargeDataService {

    private static final Logger logger = LoggerFactory.getLogger(ChargeDataService.class);
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final SimpleDateFormat DATE_ONLY_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

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

    public List<ChargeData> getChargeDataByCompanyAndDateAndUser(String company, String date, User user) {
        switch (company) {
            case "한화모티브":
                return getHanwhaMotivChargeData(date, user);
            // 다른 사업자 API 호출을 위한 케이스 추가
            default:
                throw new UnsupportedOperationException("해당 사업자의 API 호출이 지원되지 않습니다.");
        }
    }

    private List<ChargeData> getHanwhaMotivChargeData(String date, User user) {
        try {
            // 시작 날짜
            Date startDate = DATE_ONLY_FORMAT.parse(date);
            
            // 끝 날짜 (시작 날짜의 1일 후)
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(startDate);
            calendar.add(Calendar.DATE, 1);
            Date endDate = calendar.getTime();

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            headers.add("accept", "application/json, text/plain, */*");
            headers.add("sentry-trace", "79dd45738a5640cc81bcd80ba44a47e7-884904fcfe9e4744-0");
            headers.add("baggage", "sentry-environment=production,sentry-public_key=62446b6e6c99bebfc699aa22447086fe,sentry-release=com.hanwhasolutions.hanwhamotiev@1.2.3+220,sentry-trace_id=79dd45738a5640cc81bcd80ba44a47e7");
            headers.add("user-agent", "hanhwamotiev/220 CFNetwork/1496.0.7 Darwin/23.5.0");
            headers.add("accept-language", "ko-KR,ko;q=0.9");
            headers.add("authorization", "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJuZXN0b3I3OUBuYXZlci5jb20iLCJyb2xlcyI6IlVTRVIiLCJpYXQiOjE3MTAzNzQwNDZ9.WyadHyoAYfwwbqrO2b27L0mIQRj1nLExYokgUDALNeA");

            String url = String.format("https://api.hanwhamotiev.com/mypage/charge?startDate=%s&endDate=%s", 
                DATE_ONLY_FORMAT.format(startDate), DATE_ONLY_FORMAT.format(endDate));
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(url, HttpMethod.GET, entity, new ParameterizedTypeReference<Map<String, Object>>() {});

            Map<String, Object> responseBody = response.getBody();
            Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
            List<Map<String, Object>> chargeHistories = (List<Map<String, Object>>) data.get("chargeHistories");

            return chargeHistories.stream().map(this::mapToChargeData).collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching Hanwha Motive charge data", e);
            return Collections.emptyList();
        }
    }

    private ChargeData mapToChargeData(Map<String, Object> chargeHistory) {
        ChargeData chargeData = new ChargeData();
        chargeData.setCompany("한화모티브");
        chargeData.setAmountOfCharge(((Number) chargeHistory.get("value")).doubleValue());
        chargeData.setPrice(((Number) chargeHistory.get("price")).intValue());
        chargeData.setCard((String) chargeHistory.get("paymentMethod"));
        chargeData.setPoint(((Number) chargeHistory.get("pointPrice")).intValue());
        
        // 날짜 변환
        try {
            chargeData.setDate(DATE_FORMAT.parse((String) chargeHistory.get("useDate")));
        } catch (Exception e) {
            logger.error("Error parsing date", e);
        }
        // 추가 필드 설정
        return chargeData;
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
