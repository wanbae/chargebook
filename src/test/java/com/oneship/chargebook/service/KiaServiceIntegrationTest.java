package com.oneship.chargebook.service;

import com.oneship.chargebook.config.KiaProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
class KiaServiceIntegrationTest {

    @MockBean
    private ClientRegistrationRepository clientRegistrationRepository;

    @Autowired
    private KiaService kiaService;

    @Autowired
    private KiaProperties kiaProperties;

    @BeforeEach
    void setUp() {
        // 필요한 설정이 있으면 추가
    }

    @Test
    void testGetCarlist() throws Exception {
        boolean result = kiaService.getCarlist("userId");
        assertTrue(result, "Carlist should contain carId");

        // Test case for no carId in the result (you can modify the API response to test this case)
    }

    @Test
    void testGetDteJson() throws Exception {
        String result = kiaService.getDteJson("userId", false);
        assertNotNull(result);
        assertTrue(result.contains("value"), "DTE JSON should contain 'value'");
    }

    @Test
    void testGetDte() throws Exception {
        Double result = kiaService.getDte("userId");
        assertNotNull(result);
        assertTrue(result > 0, "DTE should be greater than 0");
    }

    @Test
    void testGetOdometer() throws Exception {
        long result = kiaService.getOdometer("userId");
        assertTrue(result > 0, "Odometer should be greater than 0");
    }

    @Test
    void testGetCharging() throws Exception {
        Map result = kiaService.getCharging("userId");
        assertNotNull(result);
        assertTrue(result.containsKey("soc"), "Charging JSON should contain 'soc'");
    }

    @Test
    void testGetBatteryStatus() throws Exception {
        Object result = kiaService.getBatteryStatus("userId");
        assertNotNull(result);
        assertTrue(result instanceof Number, "Battery status should be a number");
        Number soc = (Number) result;
        assertTrue(soc.intValue() >= 0 && soc.intValue() <= 100, "Battery status (soc) should be between 0 and 100");
    }
}
