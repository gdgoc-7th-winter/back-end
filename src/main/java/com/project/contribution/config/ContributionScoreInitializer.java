package com.project.contribution.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.contribution.domain.entity.ContributionScore;
import com.project.contribution.domain.repository.ContributionScoreRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ContributionScoreInitializer implements CommandLineRunner {

    private final ContributionScoreRepository scoreRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        ClassPathResource resource = new ClassPathResource("contributionScores.json");

        if (!resource.exists()) {
            log.warn("contributionScores.json not found in classpath. Skipping initialization.");
            return;
        }

        try {
            List<ContributionScore> scores = objectMapper.readValue(
                    resource.getInputStream(),
                    new TypeReference<List<ContributionScore>>() {}
            );
            for (ContributionScore score : scores) {
                upsertScore(score);
            }
            log.info("Successfully initialized scores from JSON.");

        } catch (IOException e) {
            log.error("Critical error reading contributionScores.json", e);
            throw new RuntimeException("Failed to load initial data", e);
        }
    }

    private void upsertScore(ContributionScore incomingScore) {
        try {
            scoreRepository.findByName(incomingScore.getName())
                    .ifPresentOrElse(
                            existing -> {
                                existing.update(incomingScore.getName(), incomingScore.getPoint());
                                log.debug("Updated existing score: {}", incomingScore.getName());
                            },
                            () -> {
                                scoreRepository.saveAndFlush(incomingScore);
                                log.info("Inserted new score: {}", incomingScore.getName());
                            }
                    );
        } catch (DataIntegrityViolationException e) {
            log.warn("Score '{}' was already inserted by another instance. Skipping.", incomingScore.getName());
        } catch (Exception e) {
            log.error("Unexpected error during score initialization for: {}", incomingScore.getName());
            throw e;
        }
    }
}
