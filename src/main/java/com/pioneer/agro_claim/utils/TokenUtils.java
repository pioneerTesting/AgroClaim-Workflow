package com.pioneer.agro_claim.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
public class TokenUtils {
    public String generateToken(String tokenGenerationApi, String systemUser) {

        try {
            URL url = new URL(tokenGenerationApi);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");

            String jsonPayload = "{\"userName\": \"" + systemUser + "\"}";

            try (DataOutputStream os = new DataOutputStream(connection.getOutputStream())) {
                os.writeBytes(jsonPayload);
                os.flush();
            }

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) {
                        response.append(line);
                    }
                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode jsonResponse = objectMapper.readTree(response.toString());
                    return jsonResponse.get("newGeneratedToken").asText();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
}
