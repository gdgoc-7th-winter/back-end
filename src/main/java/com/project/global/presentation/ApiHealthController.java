package com.project.global.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@Tag(name = "ApiHealth", description = "API 헬스 체크")
@RestController
@RequestMapping("/api")
public class ApiHealthController {

    @Operation(summary = "헬스 체크", description = "서버 상태 및 타임스탬프를 확인합니다.")
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of(
                "status", "UP",
                "time", Instant.now().toString()
        );
    }

    @Operation(summary = "핑", description = "서버가 응답 가능한지 확인합니다.")
    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }
}
