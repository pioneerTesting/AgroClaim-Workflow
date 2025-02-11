package com.pioneer.agro_claim.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc  // Ensures Spring MVC configurations are applied
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")  // Applies to all endpoints
                        .allowedOrigins("http://127.0.0.1:5500")  // Allows frontend requests
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")  // Allows all HTTP methods
                        .allowedHeaders("*")  // Allows all headers
                        .allowCredentials(true);  // Allows cookies and authorization headers
            }
        };
    }
}
