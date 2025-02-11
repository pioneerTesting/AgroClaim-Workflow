package com.pioneer.agro_claim.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

@UtilityClass
public class APIRequestSender {
    public static ResponseEntity<?> post(String apiUrl, String payload, String tokenGenerationApi, String systemUser) {
        try {

            System.out.println("Called api : "+apiUrl);

            TokenUtils tokenUtils = new TokenUtils();
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");
            String newToken = tokenUtils.generateToken(tokenGenerationApi, systemUser);

            if (Objects.equals(newToken, "")) {
                throw new IllegalArgumentException("Token Generation Failed");
            } else {
                connection.setRequestProperty("Authorization", newToken);
            }

            try (DataOutputStream os = new DataOutputStream(connection.getOutputStream())) {
                byte[] payloadBytes = payload.getBytes(StandardCharsets.UTF_8);
                os.write(payloadBytes);
                os.flush();
            }
            int responseCode = connection.getResponseCode();
            InputStream inputStream;
            if (responseCode == HttpURLConnection.HTTP_OK) {
                inputStream = connection.getInputStream();
            } else {
                inputStream = connection.getErrorStream();
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> jsonResponse = objectMapper.readValue(response.toString(), new TypeReference<Map<String, Object>>() {
            });
            connection.disconnect();

            System.out.println("Received "+responseCode+" from "+apiUrl);

            return ResponseEntity.status(responseCode).body(jsonResponse);

        } catch (Exception e) {

            System.out.println("Exception occurred while calling : "+apiUrl);

            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failure : " + e.getMessage());
        }
    }
}
