package com.project.contribution.infrastructure.outbox;

import com.project.contribution.application.dto.ActivityContext;
import com.project.contribution.application.port.ContributionOutboxPort;
import com.project.contribution.application.service.ContributionService;
import com.project.contribution.domain.repository.UserContributionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.contribution.infrastructure.persistence.ContributionOutboxJpaRepository;
import com.project.global.event.ActivityType;
import com.project.user.domain.entity.LevelBadge;
import com.project.user.domain.entity.User;
import com.project.user.domain.repository.LevelBadgeRepository;
import com.project.user.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class ContributionOutboxIntegrationTest {

    @MockitoBean
    private JavaMailSender javaMailSender;

    @Autowired
    private ContributionOutboxPort contributionOutboxPort;

    @Autowired
    private ContributionService contributionService;

    @Autowired
    private ContributionOutboxClaimService contributionOutboxClaimService;

    @Autowired
    private ContributionOutboxProcessor contributionOutboxProcessor;

    @Autowired
    private ContributionOutboxJpaRepository contributionOutboxJpaRepository;

    @Autowired
    private UserContributionRepository userContributionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LevelBadgeRepository levelBadgeRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long userId;

    @BeforeEach
    void setUp() {
        contributionOutboxJpaRepository.deleteAll();
        userContributionRepository.deleteAll();
        userRepository.deleteAll();

        LevelBadge dummy = levelBadgeRepository.findByPointWithinRange(0).orElseThrow();
        User user = User.builder()
                .email("outbox-" + UUID.randomUUID() + "@test.com")
                .password("pw")
                .nickname("o")
                .build();
        user.initializeLevelBadge(dummy);
        userId = userRepository.save(user).getId();
    }

    @Test
    @DisplayName("Outbox에 저장된 JSON을 역직렬화하면 applyActivity로 동일하게 지급된다")
    void deserializedOutboxPayloadAppliesSameAsDirectContext() throws Exception {
        contributionOutboxPort.append(ActivityContext.postCreated(userId, 777L));
        String payload = contributionOutboxJpaRepository.findAll().get(0).getContextPayload();
        ActivityContext deserialized = objectMapper.readValue(payload, ActivityContext.class);
        contributionService.applyActivity(deserialized);
        Integer totalPoint = jdbcTemplate.queryForObject(
                "SELECT total_point FROM users WHERE user_id = ?", Integer.class, userId);
        assertThat(totalPoint).isEqualTo(5);
    }

    @Test
    @DisplayName("Outbox append → claim → process → DONE 이고 total_point가 반영된다")
    void appendClaimProcessGrantsPoints() throws Exception {
        contributionOutboxPort.append(
                ActivityContext.postCreated(userId, 777L));

        String payload = contributionOutboxJpaRepository.findAll().get(0).getContextPayload();
        ActivityContext deserialized = objectMapper.readValue(payload, ActivityContext.class);
        assertThat(deserialized.activityType()).isEqualTo(ActivityType.POST_CREATED);
        assertThat(deserialized.subjectUserId()).isEqualTo(userId);

        List<Long> claimed = contributionOutboxClaimService.claimBatch();
        assertThat(claimed).hasSize(1);

        contributionOutboxProcessor.processAndMarkDone(claimed.getFirst());

        String outboxStatus = jdbcTemplate.queryForObject(
                "SELECT status FROM contribution_outbox WHERE id = ?", String.class, claimed.getFirst());
        assertThat(outboxStatus).isEqualTo("DONE");

        Integer totalPoint = jdbcTemplate.queryForObject(
                "SELECT total_point FROM users WHERE user_id = ?", Integer.class, userId);
        assertThat(totalPoint).isEqualTo(5);
    }

    @Test
    @DisplayName("동일 dedup_key로 append를 두 번 호출하면 한 건만 저장된다")
    void duplicateAppendIsIgnored() {
        ActivityContext ctx = ActivityContext.postCreated(userId, 888L);
        contributionOutboxPort.append(ctx);
        contributionOutboxPort.append(ctx);
        assertThat(contributionOutboxJpaRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("회수 활동도 Outbox 처리 후 원자 갱신된다")
    void revokeFlow() {
        contributionOutboxPort.append(ActivityContext.postCreated(userId, 999L));
        Long id = contributionOutboxClaimService.claimBatch().getFirst();
        contributionOutboxProcessor.processAndMarkDone(id);

        contributionOutboxPort.append(ActivityContext.postDeleted(userId, 999L));
        Long id2 = contributionOutboxClaimService.claimBatch().getFirst();
        contributionOutboxProcessor.processAndMarkDone(id2);

        User u = userRepository.findActiveByIdLean(userId).orElseThrow();
        assertThat(u.getTotalPoint()).isZero();
    }
}
