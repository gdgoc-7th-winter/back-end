package com.project.global.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@Tag(name = "Health", description = "Health Check API")
@RestController
@RequestMapping("/api")
public class HealthController {

    @Operation(summary = "Health Check", description = "서버 상태를 확인합니다.")
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of(
                "status", "UP",
                "time", Instant.now().toString()
        );
    }

    @Operation(summary = "Ping", description = "서버가 응답 가능한지 확인합니다.")
    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }
}
