package com.devbrief.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final String[] frontendOrigins;

    public WebConfig(@Value("${devbrief.frontend-origin:http://localhost:3000,http://127.0.0.1:3000}") String frontendOrigin) {
        this.frontendOrigins = Arrays.stream(frontendOrigin.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isBlank())
                .toArray(String[]::new);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(frontendOrigins)
                .allowedMethods("GET", "POST", "OPTIONS")
                .allowedHeaders("*");
    }
}
