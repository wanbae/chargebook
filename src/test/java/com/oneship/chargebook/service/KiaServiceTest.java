package com.oneship.chargebook.service;

import com.oneship.chargebook.config.KiaProperties;
import com.oneship.chargebook.model.KiaToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class KiaServiceTest {

    @Mock
    private KiaProperties kiaProperties;

    @InjectMocks
    private KiaService kiaService;

    private KiaToken kiaToken;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(kiaProperties.getCarId()).thenReturn("65169010-30e1-4c7e-91e4-f747091e2268");
        when(kiaProperties.getRefreshToken()).thenReturn("0BOB0PMPXOANI4PJ9QARUQ");
        when(kiaProperties.getApi()).thenReturn(new KiaProperties.Api() {{
            setBaseUrl("https://dev.kr-ccapi.kia.com/api/v1");
            setTokenUrl("https://prd.kr-ccapi.kia.com/api/v1/user/oauth2/token");
        }});
        when(kiaProperties.getClientId()).thenReturn("e8eab39e-cd97-4eec-9c5b-c238cb0225d2");
        when(kiaProperties.getClientSecret()).thenReturn("oy42brpiFedstigC8i2GLu33Zie0SUq7EVaeTmHMwt0EFiLh");

        // 초기화 토큰 설정
        kiaToken = new KiaToken("initialAccessToken", "initialRefreshToken", "Bearer", 86400);
        kiaToken.setExpirationTime(LocalDateTime.now().plusSeconds(86400));
    }

    @Test
    void testKiaProperties() {
        assertEquals("65169010-30e1-4c7e-91e4-f747091e2268", kiaProperties.getCarId());
        assertEquals("0BOB0PMPXOANI4PJ9QARUQ", kiaProperties.getRefreshToken());
        assertEquals("https://dev.kr-ccapi.kia.com/api/v1", kiaProperties.getApi().getBaseUrl());
        assertEquals("https://prd.kr-ccapi.kia.com/api/v1/user/oauth2/token", kiaProperties.getApi().getTokenUrl());
        assertEquals("e8eab39e-cd97-4eec-9c5b-c238cb0225d2", kiaProperties.getClientId());
        assertEquals("oy42brpiFedstigC8i2GLu33Zie0SUq7EVaeTmHMwt0EFiLh", kiaProperties.getClientSecret());
    }

    @Test
    void testGetDteJson() throws Exception {
        // Mock executeApiCall method
        String expectedResponse = "{\"value\":345}";
        KiaService spyService = spy(kiaService);
        doReturn(expectedResponse).when(spyService).executeApiCall(anyString(), anyString());

        String result = spyService.getDteJson(false);
        assertNotNull(result);
        assertTrue(result.contains("345"));
    }

    @Test
    void testGetDte() throws Exception {
        // Mock executeApiCall method
        String expectedResponse = "{\"value\":345}";
        KiaService spyService = spy(kiaService);
        doReturn(expectedResponse).when(spyService).executeApiCall(anyString(), anyString());

        Double result = spyService.getDte();
        assertEquals(345.0, result);
    }

    @Test
    void testGetOdometer() throws Exception {
        // Mock executeApiCall method
        String expectedResponse = "{\"odometers\":[{\"value\":6968}]}";
        KiaService spyService = spy(kiaService);
        doReturn(expectedResponse).when(spyService).executeApiCall(anyString(), anyString());

        long result = spyService.getOdometer();
        assertEquals(6968, result);
    }

    @Test
    void testGetCharging() throws Exception {
        // Mock executeApiCall method
        String expectedResponse = "{\"soc\":78}";
        KiaService spyService = spy(kiaService);
        doReturn(expectedResponse).when(spyService).executeApiCall(anyString(), anyString());

        Map result = spyService.getCharging();
        assertEquals(78d, result.get("soc"));
    }

    @Test
    void testGetBatteryStatus() throws Exception {
        // Mock executeApiCall method
        String expectedResponse = "{\"soc\":78}";
        KiaService spyService = spy(kiaService);
        doReturn(expectedResponse).when(spyService).executeApiCall(anyString(), anyString());

        Object result = spyService.getBatteryStatus();
        assertEquals(78d, result);
    }

    @Test
    void testGetAccessToken() throws Exception {
        // Mock tokenAPICall method
        String expectedResponse = "{\"access_token\":\"test_access_token\"}";
        KiaService spyService = spy(kiaService);
        doReturn(expectedResponse).when(spyService).tokenAPICall(anyString());

        String result = spyService.getAccessToken();
        assertEquals("test_access_token", result);
    }

    @Test
    void testGetCarlist() throws Exception {
        // Mock executeApiCall method
        String expectedResponse = "{\"carId\":\"65169010-30e1-4c7e-91e4-f747091e2268\"}";
        KiaService spyService = spy(kiaService);
        doReturn(expectedResponse).when(spyService).executeApiCall(anyString(), anyString());

        boolean result = spyService.getCarlist();
        assertTrue(result);

        // Test case for empty access token
        doReturn("").when(spyService).getAccessToken();
        result = spyService.getCarlist();
        assertFalse(result);

        // Test case for no carId in the result
        expectedResponse = "{\"someOtherKey\":\"someValue\"}";
        doReturn(expectedResponse).when(spyService).executeApiCall(anyString(), anyString());
        result = spyService.getCarlist();
        assertFalse(result);
    }
}
