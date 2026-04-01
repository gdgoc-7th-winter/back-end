package com.project.contribution.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.contribution.domain.entity.ContributionScore;
import com.project.contribution.domain.repository.ContributionScoreRepository;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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
    public void run(String... args) throws Exception {
        ClassPathResource resource = new ClassPathResource("contributionScores.json");

        if (!resource.exists()) {
            log.warn("classpath에서 contributionScores.json을 찾을 수 없습니다. 초기화를 건너뜁니다.");
            return;
        }

        try {
            List<ContributionScoreData> scores = objectMapper.readValue(
                    resource.getInputStream(),
                    new TypeReference<List<ContributionScoreData>>() {}
            );
            for (ContributionScoreData data : scores) {
                ContributionScore score = ContributionScore.builder()
                        .code(data.code())
                        .name(data.name())
                        .point(data.point())
                        .build();
                upsertScore(score);
            }
            log.info("contributionScores.json 기반 점수 초기화를 완료했습니다.");

        } catch (IOException e) {
            log.error("contributionScores.json 읽기 중 오류가 발생했습니다.", e);
            throw new RuntimeException("초기 데이터를 불러오지 못했습니다.", e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void upsertScore(ContributionScore incomingScore) {
        try {
            scoreRepository.findByCode(incomingScore.getCode())
                    .ifPresentOrElse(
                            existing -> {
                                existing.update(incomingScore.getName(), incomingScore.getPoint());
                                log.debug("기존 점수 항목을 갱신했습니다: {}", incomingScore.getCode());
                            },
                            () -> {
                                scoreRepository.saveAndFlush(incomingScore);
                                log.info("새 점수 항목을 추가했습니다: {}", incomingScore.getCode());
                            }
                    );
        } catch (DataIntegrityViolationException e) {
            log.warn("점수 항목 '{}'은(는) 다른 인스턴스에서 이미 삽입되었습니다. 건너뜁니다.", incomingScore.getCode());
        } catch (Exception e) {
            log.error("점수 항목 초기화 중 예기치 않은 오류가 발생했습니다: {}", incomingScore.getCode());
            throw e;
        }
    }
}
