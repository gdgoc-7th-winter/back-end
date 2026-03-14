package com.project.user.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.user.domain.entity.LevelBadge;
import com.project.user.domain.repository.LevelBadgeRepository;
import com.project.user.presentation.dto.request.LevelBadgeRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class LevelBadgeInitializer implements CommandLineRunner {

    private final LevelBadgeRepository levelBadgeRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // 이미 데이터가 있다면 초기화하지 않음 (중복 방지)
        if (levelBadgeRepository.count() > 0) {
            log.info("Level badges already initialized. Skipping.");
            return;
        }
        ClassPathResource resource = new ClassPathResource("levelBadges.json");

        if (!resource.exists()) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Level badges file not found.");
        }

        // 1. JSON 파일 읽기
        try (InputStream is = resource.getInputStream()) {
            List<LevelBadgeRequest> badgeRequests = objectMapper.readValue(is, new TypeReference<>() {});

            if (badgeRequests == null || badgeRequests.isEmpty()) {
                throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "levelBadges.json is empty or invalid.");
            }

            // 2. DTO -> Entity 변환
            List<LevelBadge> badges = badgeRequests.stream()
                    .map(req -> new LevelBadge(
                            req.levelName(),
                            req.levelDescription(),
                            req.levelImage(),
                            req.minimumPoint(),
                            req.maximumPoint()
                    ))
                    .toList();

            // 3. DB 저장
            levelBadgeRepository.saveAll(badges);
            log.info("Successfully initialized level badges from JSON.");
        }
        catch (IOException e) {
            throw new IllegalStateException("Failed to parse JSON file. check the JSON format", e);
        }
        catch (Exception e) {
            throw new RuntimeException("Unexpected error during initialization."+ e.getMessage(), e);
        }
    }
}
