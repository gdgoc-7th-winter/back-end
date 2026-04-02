package com.project.contribution.infrastructure.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.contribution.application.dto.ActivityContext;
import com.project.contribution.application.service.ContributionService;
import com.project.contribution.domain.ContributionOutboxStatus;
import com.project.contribution.infrastructure.persistence.ContributionOutboxJpaRepository;
import com.project.contribution.infrastructure.persistence.entity.ContributionOutboxEntity;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class ContributionOutboxProcessor {

    private final ContributionOutboxJpaRepository contributionOutboxJpaRepository;
    private final ObjectMapper objectMapper;
    private final ContributionService contributionService;

    /**
     * Spring Data {@code SimpleJpaRepository}는 클래스 단위 {@code readOnly=true}라서,
     * 같은 트랜잭션에 조인될 때 읽기 전용으로 남아 outbox 갱신이 커밋되지 않을 수 있다.
     * 이 메서드는 명시적으로 {@code readOnly=false}로 쓰기 트랜잭션을 보장한다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false)
    public void processAndMarkDone(Long id) {
        ContributionOutboxEntity row = contributionOutboxJpaRepository.findById(id).orElseThrow();
        if (row.getStatus() != ContributionOutboxStatus.PROCESSING) {
            return;
        }
        ActivityContext context;
        try {
            context = objectMapper.readValue(row.getContextPayload(), ActivityContext.class);
        } catch (IOException e) {
            throw new IllegalStateException("Outbox payload 역직렬화 실패 id=" + id, e);
        }
        contributionService.applyActivity(context);
        row.markDone(Instant.now());
        contributionOutboxJpaRepository.save(row);
    }
}
