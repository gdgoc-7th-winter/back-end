package com.project.global.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class RestClientConfig {

    @Value("${piston.api.url}")
    private String pistonApiUrl;

    @Value("${piston.api.timeout-seconds:15}")
    private int timeoutSeconds;

    @PostConstruct
    public void validatePistonApiUrl() {
        if (pistonApiUrl == null || pistonApiUrl.isBlank()) {
            throw new IllegalStateException("piston.api.url is not configured. Set the PISTON_API_URL environment variable.");
        }
    }

    @Bean
    public RestClient pistonClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(timeoutSeconds));

        return RestClient.builder()
                .baseUrl(pistonApiUrl)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .requestFactory(factory)
                .build();
    }
}
