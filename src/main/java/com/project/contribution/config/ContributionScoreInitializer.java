package com.project.contribution.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.contribution.domain.entity.ContributionScore;
import com.project.contribution.domain.repository.ContributionScoreRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ContributionScoreInitializer implements CommandLineRunner {

    private final ContributionScoreRepository scoreRepository;
    private final ObjectMapper objectMapper; // JSON 파싱용
    Logger log = LoggerFactory.getLogger(ContributionScoreInitializer.class);

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (scoreRepository.count() > 0) return;

        // 리소스 경로에서 파일 로드
        ClassPathResource resource = new ClassPathResource("contributionScores.json");

        if (!resource.exists()) {
            log.warn("contributionScores.json not found in classpath. Skipping initialization.");
            return;
        }

        // JSON을 리스트 형태로 변환
        try {
            List<ContributionScore> scores = objectMapper.readValue(
                    resource.getInputStream(),
                    new TypeReference<List<ContributionScore>>() {}
            );
            scoreRepository.saveAll(scores);
            log.info("Successfully initialized scores from JSON.");

        } catch (IOException | RuntimeException e) {
            log.error("Failed to initialize scores due to: {}. Application will continue to start.",
                    e.getMessage(), e);
        }
    }
}
