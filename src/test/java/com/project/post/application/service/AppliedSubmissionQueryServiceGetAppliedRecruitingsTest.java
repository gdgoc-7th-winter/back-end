package com.project.post.application.service;

import com.project.post.application.dto.RecruitingPost.AppliedRecruitingPostSummaryResponse;
import com.project.post.application.service.impl.RecruitingPost.ApplicationSubmissionQueryServiceImpl;
import com.project.post.domain.enums.RecruitingCategory;
import com.project.post.domain.enums.RecruitingStatus;
import com.project.post.domain.repository.ApplicationSubmissionRepository;
import com.project.post.domain.repository.dto.AppliedRecruitingPostListQueryResult;
import com.project.user.domain.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.Mockito;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ApplicationSubmissionQueryServiceGetAppliedRecruitingsTest {

    @Mock private ApplicationSubmissionRepository repo;

    @InjectMocks
    private ApplicationSubmissionQueryServiceImpl service;

    @Test
    @DisplayName("status 필터 전달 확인")
    void passesStatus() {
        User user = Mockito.mock(User.class);
        given(user.getId()).willReturn(1L);

        given(repo.findAppliedRecruitingPostListByUserId(
                Mockito.eq(1L),
                Mockito.eq(RecruitingStatus.OPEN),
                Mockito.any(),
                Mockito.any()
        )).willReturn(new PageImpl<>(List.of()));

        service.getAppliedRecruitings(user, RecruitingStatus.OPEN, PageRequest.of(0, 10));

        Mockito.verify(repo).findAppliedRecruitingPostListByUserId(
                Mockito.eq(1L),
                Mockito.eq(RecruitingStatus.OPEN),
                Mockito.any(),
                Mockito.any()
        );
    }

    @Test
    @DisplayName("상태 및 라벨 매핑")
    void mapsStatus() {
        User user = Mockito.mock(User.class);
        given(user.getId()).willReturn(1L);

        Instant now = Instant.now();

        AppliedRecruitingPostListQueryResult result =
                new AppliedRecruitingPostListQueryResult(
                        1L,
                        RecruitingCategory.STUDY,
                        now.minus(1, ChronoUnit.DAYS),
                        now.plus(2, ChronoUnit.DAYS),
                        now,
                        1L,
                        "제목",
                        "내용",
                        null,
                        1L,
                        "작성자",
                        null,
                        "컴공",
                        "백엔드",
                        null,
                        false,
                        1,1,1,1,
                        now
                );

        given(repo.findAppliedRecruitingPostListByUserId(
                Mockito.eq(1L),
                Mockito.isNull(),
                Mockito.any(),
                Mockito.any()
        )).willReturn(new PageImpl<>(List.of(result)));

        Page<AppliedRecruitingPostSummaryResponse> res =
                service.getAppliedRecruitings(user, null, PageRequest.of(0, 10));

        assertThat(res.getContent())
                .singleElement()
                .satisfies(r -> {
                    assertThat(r.status()).isEqualTo(RecruitingStatus.OPEN);
                    assertThat(r.statusLabel()).startsWith("D-");
                });
    }
}
