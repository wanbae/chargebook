package com.oneship.chargebook.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oneship.chargebook.config.KiaProperties;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class KiaService {
    private static final Logger logger = LoggerFactory.getLogger(KiaService.class);

    @Autowired
    private KiaProperties kiaProperties;

    public String getDteJson(boolean prettyPrinting) throws Exception {
        String accessToken = getAccessToken();
        if (accessToken.isEmpty()) {
            return "";
        }
        String apiURL = kiaProperties.getApi().getBaseUrl() + "/car/status/" + kiaProperties.getCarId() + "/dte";
        String result = executeApiCall(apiURL, accessToken);

        if (prettyPrinting && result != null) {
            result = toJson(fromJson(result, Map.class));
        }

        logger.info("주행 가능 거리: {}", result);
        return result;
    }

    public Double getDte() throws Exception {
        String accessToken = getAccessToken();
        if (accessToken.isEmpty()) {
            return 0D;
        }
        String result = getDteJson(false);
        Map map = fromJson(result, Map.class);
        return (Double) map.get("value");
    }

    public long getOdometer() throws Exception {
        String accessToken = getAccessToken();
        if (accessToken.isEmpty()) {
            return 0;
        }
        String odometer = getOdometerJson(false);
        Map map = fromJson(odometer, Map.class);
        Object odometers = map.get("odometers");
        if (odometers != null && odometers instanceof List) {
            List odo = (List) odometers;
            Map odoMap = (Map) odo.get(0);
            Object value = odoMap.get("value");
            if (value != null) {
                float v = Float.parseFloat("" + value);
                return (long) v;
            }
        }
        return 0;
    }

    public String getOdometerJson(boolean prettyPrinting) throws Exception {
        String accessToken = getAccessToken();
        if (accessToken.isEmpty()) {
            return "";
        }
        String apiURL = kiaProperties.getApi().getBaseUrl() + "/car/status/" + kiaProperties.getCarId() + "/odometer";
        String result = executeApiCall(apiURL, accessToken);
        if (prettyPrinting && result != null) {
            result = toJson(fromJson(result, Map.class));
        }
        logger.info("누적 운행 거리: {}", result);
        return result;
    }

    public String getChargingJson(boolean prettyPrinting) throws Exception {
        String accessToken = getAccessToken();
        if (accessToken.isEmpty()) {
            return "";
        }
        String apiURL = kiaProperties.getApi().getBaseUrl() + "/car/status/" + kiaProperties.getCarId() + "/ev/charging";
        String result = executeApiCall(apiURL, accessToken);
        if (prettyPrinting && result != null) {
            result = toJson(fromJson(result, Map.class));
        }
        logger.info("충전 상태: {}", result);
        return result;
    }

    public boolean getCarlist() throws Exception {
        String accessToken = getAccessToken();
        if (accessToken.isEmpty()) {
            return false;
        }
        String apiURL = kiaProperties.getApi().getBaseUrl() + "/car/profile/carlist";
        String result = executeApiCall(apiURL, accessToken);
        logger.info("차량 리스트: {}", result);
        return result.contains("carId");
    }

    public Map getCharging() throws Exception {
        String accessToken = getAccessToken();
        if (accessToken.isEmpty()) {
            return Collections.emptyMap();
        }
        String result = getChargingJson(false);
        return fromJson(result, Map.class);
    }

    public String getBatteryStatusJson(boolean prettyPrinting) throws Exception {
        String accessToken = getAccessToken();
        if (accessToken.isEmpty()) {
            return "";
        }
        String apiURL = kiaProperties.getApi().getBaseUrl() + "/car/status/" + kiaProperties.getCarId() + "/ev/battery";
        String result = executeApiCall(apiURL, accessToken);
        if (prettyPrinting && result != null) {
            result = toJson(fromJson(result, Map.class));
        }
        logger.info("전기차 배터리 잔량: {}", result);
        return result;
    }

    public Object getBatteryStatus() throws Exception {
        String result = getBatteryStatusJson(false);
        logger.info("전기차 배터리 잔량: {}", result);
        return fromJson(result, Map.class).get("soc");
    }

    protected String executeApiCall(String apiURL, String accessToken) {
        StringBuilder sb = new StringBuilder();
        String responseData;

        String token = "Bearer " + accessToken;
        String contentType = "application/json";

        try {
            URL url = new URL(apiURL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Authorization", token);
            con.setRequestProperty("Content-Type", contentType);
            int responseCode = con.getResponseCode();
            BufferedReader br;
            if (responseCode == HttpURLConnection.HTTP_OK) {
                br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            } else {
                br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            }
            while ((responseData = br.readLine()) != null) {
                sb.append(responseData);
            }
            br.close();
        } catch (Exception e) {
            logger.error("Error during API call", e);
        }
        return sb.toString();
    }

    protected String getAccessToken() throws Exception {
        String refreshToken = kiaProperties.getRefreshToken();
        String requestBody = "grant_type=refresh_token&refresh_token=" + refreshToken + "&redirect_uri=https://bi3eh7q4crerqn52ieauexuufa0mtyim.lambda-url.ap-northeast-2.on.aws";
        String tokenResponse = tokenAPICall(requestBody);
        ObjectMapper accessTokenObjectMapper = new ObjectMapper();
        JsonNode TokenRoot = accessTokenObjectMapper.readTree(tokenResponse);
        String _accessToken = TokenRoot.path("access_token").asText();
        logger.info("accessToken = {}", _accessToken);
        return _accessToken;
    }

    protected String tokenAPICall(String requestBody) {
        StringBuilder sb = new StringBuilder();
        String responseData;

        String apiURL = kiaProperties.getApi().getTokenUrl();
        String clientId = kiaProperties.getClientId();
        String clientSecret = kiaProperties.getClientSecret();
        String token = "Basic " + Base64.encodeBase64String((clientId + ":" + clientSecret).getBytes());
        String contentType = "application/x-www-form-urlencoded";

        try {
            URL url = new URL(apiURL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Authorization", token);
            con.setRequestProperty("Content-Type", contentType);
            con.setDoOutput(true);
            try (DataOutputStream output = new DataOutputStream(con.getOutputStream())) {
                output.writeBytes(requestBody);
                output.flush();
            } catch (Exception e) {
                logger.error("Error writing to output stream", e);
            }
            int responseCode = con.getResponseCode();
            BufferedReader br;
            if (responseCode == HttpURLConnection.HTTP_OK) {
                br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            } else {
                br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            }
            while ((responseData = br.readLine()) != null) {
                sb.append(responseData);
            }
            br.close();
            logger.info("responseCode = {}", responseCode);
            logger.info("responseData = {}", sb.toString());
        } catch (Exception e) {
            logger.error("Error during token API call", e);
        }
        return sb.toString();
    }

    private <T> T fromJson(String json, Class<T> t) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.fromJson(json, t);
    }

    private String toJson(Object obj) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(obj);
    }
}
