package com.pioneer.agro_claim.filters;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;

@Component
@WebFilter(urlPatterns = "/*")
@Order(1)
public class AuthenticationFilter implements Filter {
    @Value("${authentication.connection}")
    private String authenticationUrl;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpServletRequest = new MultiReadHttpServletRequestWrapper((HttpServletRequest) request);
        String token = httpServletRequest.getHeader("Authorization");
        String authenticateUsername = getAuthenticateUsernameFromPayload(httpServletRequest);
        if(authenticateUsername == null){
            String jsonResponse = "{\"status\":\"Failure\",\"message\":\"Invalid JSON. Please verify it first before sending any request.\"}";
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.setContentType("application/json");
            httpResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(jsonResponse);
            return;
        }
        if (!isValidUsername(authenticateUsername, token)) {
            String jsonResponse = "{\"status\":\"Failure\",\"message\":\"Token Verification Failed\"}";

            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.setContentType("application/json");
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(jsonResponse);
            return;
        }

        chain.doFilter(httpServletRequest, response);
    }

    private String getAuthenticateUsernameFromPayload(HttpServletRequest request) {
        try {
            String requestBody = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode payload = objectMapper.readTree(requestBody);
            return payload.has("authenticateUsername") ? payload.get("authenticateUsername").asText() : null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean isValidUsername(String authenticateUsername, String token) {

        String verifyTokenApi = authenticationUrl;

        try {
            String verifyTokenUrl = UriComponentsBuilder
                    .fromUriString(verifyTokenApi)
                    .queryParam("userName", authenticateUsername)
                    .build()
                    .toUriString();
            HttpHeaders headers = new HttpHeaders();
            headers.add("authorization", token);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> verifyTokenResponse = restTemplate.exchange(
                    verifyTokenUrl,
                    HttpMethod.POST,
                    entity,
                    String.class
            );
            return verifyTokenResponse.getStatusCode() == HttpStatus.OK;
        }
        catch (HttpClientErrorException.Unauthorized unauthorizedException) {
            return false;
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Initialization code if needed
    }

    @Override
    public void destroy() {
        // Cleanup code if needed
    }

    private static class MultiReadHttpServletRequestWrapper extends HttpServletRequestWrapper {
        private final String body;

        public MultiReadHttpServletRequestWrapper(HttpServletRequest request) throws IOException {
            super(request);
            this.body = getRequestBody(request);
        }

        @Override
        public BufferedReader getReader() throws IOException {
            return new BufferedReader(new InputStreamReader(getInputStream()));
        }

        @Override
        public ServletInputStream getInputStream() throws IOException {
            final byte[] bytes = body.getBytes();
            return new ServletInputStream() {
                private int index = 0;

                @Override
                public boolean isFinished() {
                    return index == bytes.length;
                }

                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setReadListener(ReadListener readListener) {
                    // Not implemented for simplicity
                }

                @Override
                public int read() throws IOException {
                    return index < bytes.length ? bytes[index++] : -1;
                }
            };
        }

        private String getRequestBody(HttpServletRequest request) throws IOException {
            try (BufferedReader bufferedReader = request.getReader()) {
                return bufferedReader.lines().collect(Collectors.joining(System.lineSeparator()));
            }
        }
    }
}
