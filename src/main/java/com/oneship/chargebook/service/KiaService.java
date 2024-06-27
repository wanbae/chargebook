package com.oneship.chargebook.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.oneship.chargebook.config.KiaProperties;
import com.oneship.chargebook.model.KiaToken;
import com.oneship.chargebook.repository.KiaTokenRepository;
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
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Service
public class KiaService {
    private static final Logger logger = LoggerFactory.getLogger(KiaService.class);

    @Autowired
    private KiaProperties kiaProperties;

    @Autowired
    private KiaTokenRepository kiaTokenRepository;
    
    public KiaToken getKiaTokenByUserId(Long userId) {
        return kiaTokenRepository.findById(userId.toString()).orElse(null);
    }
    public KiaToken requestKiaToken(Long userId, String authorizationCode) {
        String requestBody = "grant_type=authorization_code&code=" + authorizationCode + "&redirect_uri=" + kiaProperties.getRedirectUri();
        String tokenResponse = tokenAPICall(requestBody);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode tokenRoot = objectMapper.readTree(tokenResponse);
            String accessToken = tokenRoot.path("access_token").asText();
            String refreshToken = tokenRoot.path("refresh_token").asText();
            String tokenType = tokenRoot.path("token_type").asText();
            int expiresIn = tokenRoot.path("expires_in").asInt();
            KiaToken kiaToken = new KiaToken(userId.toString(), accessToken, refreshToken, tokenType, expiresIn, authorizationCode);
            kiaTokenRepository.save(kiaToken);
            return kiaToken;
        } catch (Exception e) {
            logger.error("Error parsing token response", e);
            return null;
        }
    }

    public String getAccessToken(Long userId) throws Exception {
        Optional<KiaToken> kiaTokenOptional = kiaTokenRepository.findById(userId.toString());
        if (kiaTokenOptional.isPresent()) {
            KiaToken kiaToken = kiaTokenOptional.get();
            if (kiaToken.isTokenExpired()) {
                kiaToken = refreshKiaToken(userId);
            }
            return kiaToken.getAccessToken();
        } else {
            throw new Exception("No token found for user: " + userId);
        }
    }

    public long getOdometer(Long userId) throws Exception {
        String accessToken = getAccessToken(userId);
        if (accessToken.isEmpty()) {
            return 0;
        }
        String apiURL = kiaProperties.getApi().getBaseUrl() + "/car/status/" + kiaProperties.getCarId() + "/odometer";
        String result = executeApiCall(apiURL, accessToken);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(result);
        return rootNode.path("odometers").get(0).path("value").asLong();
    }

    public double getBatteryStatus(Long userId) throws Exception {
        String accessToken = getAccessToken(userId);
        if (accessToken.isEmpty()) {
            return 0.0;
        }
        String apiURL = kiaProperties.getApi().getBaseUrl() + "/car/status/" + kiaProperties.getCarId() + "/ev/battery";
        String result = executeApiCall(apiURL, accessToken);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(result);
        return rootNode.path("soc").asDouble();
    }

    public double getDte(Long userId) throws Exception {
        String accessToken = getAccessToken(userId);
        if (accessToken.isEmpty()) {
            return 0.0;
        }
        String apiURL = kiaProperties.getApi().getBaseUrl() + "/car/status/" + kiaProperties.getCarId() + "/dte";
        String result = executeApiCall(apiURL, accessToken);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(result);
        return rootNode.path("value").asDouble();
    }

    public String getDteJson(Long userId, boolean forceUpdate) throws Exception {
        String accessToken = getAccessToken(userId);
        if (accessToken.isEmpty()) {
            return "{}";
        }
        String apiURL = kiaProperties.getApi().getBaseUrl() + "/car/status/" + kiaProperties.getCarId() + "/dte";
        return executeApiCall(apiURL, accessToken);
    }

    public Map<String, Object> getCharging(Long userId) throws Exception {
        String accessToken = getAccessToken(userId);
        if (accessToken.isEmpty()) {
            return Collections.emptyMap();
        }
        String apiURL = kiaProperties.getApi().getBaseUrl() + "/car/status/" + kiaProperties.getCarId() + "/charging";
        String result = executeApiCall(apiURL, accessToken);
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(result, Map.class);
    }

    public boolean getCarlist(Long userId) throws Exception {
        String accessToken = getAccessToken(userId);
        if (accessToken.isEmpty()) {
            return false;
        }
        String apiURL = kiaProperties.getApi().getBaseUrl() + "/car/list";
        String result = executeApiCall(apiURL, accessToken);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(result);
        return rootNode.has("carId");
    }

    protected KiaToken refreshKiaToken(Long userId) throws Exception {
        KiaToken currentToken = kiaTokenRepository.findById(userId.toString())
                .orElseThrow(() -> new Exception("No token found for user: " + userId));

        String refreshToken = currentToken.getRefreshToken();
        String requestBody = "grant_type=refresh_token&refresh_token=" + refreshToken + "&redirect_uri=" + kiaProperties.getRedirectUri();
        String tokenResponse = tokenAPICall(requestBody);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode tokenRoot = objectMapper.readTree(tokenResponse);

        String accessToken = tokenRoot.path("access_token").asText();
        String newRefreshToken = tokenRoot.path("refresh_token").asText();
        String tokenType = tokenRoot.path("token_type").asText();
        int expiresIn = tokenRoot.path("expires_in").asInt();

        KiaToken kiaToken = new KiaToken(userId.toString(), accessToken, newRefreshToken, tokenType, expiresIn, currentToken.getCode());
        kiaToken.setExpirationTime(LocalDateTime.now().plusSeconds(expiresIn));
        kiaTokenRepository.save(kiaToken);
        logger.info("New accessToken = {}", accessToken);
        return kiaToken;
    }

    public void deleteKiaToken(Long userId) throws Exception {
        KiaToken kiaToken = kiaTokenRepository.findById(userId.toString()).orElse(null);
        if (kiaToken == null || kiaToken.getAccessToken() == null) {
            return;
        }

        String requestBody = "grant_type=delete&access_token=" + kiaToken.getAccessToken();
        tokenAPICall(requestBody);

        kiaTokenRepository.deleteById(userId.toString());
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

    protected String executeApiCall(String apiUrl, String accessToken) {
        StringBuilder sb = new StringBuilder();
        String responseData;

        try {
            URL url = new URL(apiUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Authorization", "Bearer " + accessToken);
            con.setRequestProperty("Content-Type", "application/json");
            con.setDoOutput(true);
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
            logger.error("Error during API call", e);
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
