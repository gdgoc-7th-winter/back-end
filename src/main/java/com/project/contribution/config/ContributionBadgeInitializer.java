package com.project.contribution.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.contribution.domain.entity.ContributionBadge;
import com.project.contribution.domain.repository.ContributionBadgeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ContributionBadgeInitializer implements CommandLineRunner {

    private final ContributionBadgeRepository badgeRepository;
    private final ObjectMapper objectMapper; // JSON 파싱용

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (badgeRepository.count() > 0) return;

        // 리소스 경로에서 파일 로드
        ClassPathResource resource = new ClassPathResource("contributionBadges.json");

        // JSON을 리스트 형태로 변환
        List<ContributionBadge> badges = objectMapper.readValue(
                resource.getInputStream(),
                new TypeReference<List<ContributionBadge>>() {}
        );

        badgeRepository.saveAll(badges);
    }
}
