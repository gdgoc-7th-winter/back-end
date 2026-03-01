package com.project.global.presentation;

import com.project.global.presentation.swagger.ApiHealthControllerDocs;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiHealthController implements ApiHealthControllerDocs {

    @Override
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of(
                "status", "UP",
                "time", Instant.now().toString()
        );
    }

    @Override
    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }
}
