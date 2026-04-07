package com.project.post.application.service;

import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.post.application.dto.RecruitingPost.ApplicationSubmissionSummaryResponse;
import com.project.post.application.service.impl.RecruitingPost.ApplicationSubmissionQueryServiceImpl;
import com.project.post.domain.entity.RecruitingPost;
import com.project.post.domain.entity.Post;
import com.project.post.domain.entity.RecruitingApplication;
import com.project.post.domain.entity.ApplicationSubmission;

import com.project.post.domain.enums.ApplicationSubmissionSortType;
import com.project.post.domain.enums.Campus;
import com.project.post.domain.repository.ApplicationSubmissionRepository;
import com.project.post.domain.repository.RecruitingPostRepository;
import com.project.post.domain.repository.RecruitingApplicationRepository;

import com.project.user.domain.entity.Department;
import com.project.user.domain.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.ArgumentCaptor;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Page;

import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class ApplicationSubmissionQueryServiceGetSubmissionListTest {

    @Mock private ApplicationSubmissionRepository repo;
    @Mock private RecruitingPostRepository postRepo;
    @Mock private RecruitingApplicationRepository appRepo;

    @InjectMocks
    private ApplicationSubmissionQueryServiceImpl service;

    @Test
    @DisplayName("작성자만 조회 가능")
    void onlyAuthorAllowed() {
        RecruitingPost post = mock(RecruitingPost.class);
        Post base = mock(Post.class);
        User author = mock(User.class);
        User user = mock(User.class);

        given(postRepo.findByIdAndDeletedAtIsNull(1L)).willReturn(Optional.of(post));
        given(post.getPost()).willReturn(base);
        given(base.getAuthor()).willReturn(author);
        given(author.getId()).willReturn(1L);
        given(user.getId()).willReturn(2L);

        assertThatThrownBy(() ->
                service.getSubmissionList(1L, user, null, null, null,
                        ApplicationSubmissionSortType.LATEST, PageRequest.of(0, 10))
        ).isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ACCESS_DENIED);
    }

    @Test
    @DisplayName("페이지 크기 제한 적용")
    void capsPageSize() {
        RecruitingPost post = mock(RecruitingPost.class);
        RecruitingApplication app = mock(RecruitingApplication.class);
        Post base = mock(Post.class);
        User user = mock(User.class);

        given(postRepo.findByIdAndDeletedAtIsNull(1L)).willReturn(Optional.of(post));
        given(post.getPost()).willReturn(base);
        given(base.getAuthor()).willReturn(user);
        given(user.getId()).willReturn(1L);
        given(appRepo.findByRecruitingPost(post)).willReturn(Optional.of(app));
        given(repo.findAll(
                ArgumentMatchers.<org.springframework.data.jpa.domain.Specification<ApplicationSubmission>>any(),
                Mockito.any(Pageable.class)
        )).willReturn(new PageImpl<ApplicationSubmission>(
                List.of(),
                PageRequest.of(0, 100),
                0
        ));

        service.getSubmissionList(1L, user, null, null, null,
                ApplicationSubmissionSortType.LATEST, PageRequest.of(0, 500));

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        Mockito.verify(repo).findAll(
                ArgumentMatchers.<org.springframework.data.jpa.domain.Specification<ApplicationSubmission>>any(),
                captor.capture()
        );

        assertThat(captor.getValue().getPageSize()).isEqualTo(100);
    }

    @Test
    @DisplayName("응답 매핑 확인")
    void mapsResponse() {
        RecruitingPost post = mock(RecruitingPost.class);
        RecruitingApplication app = mock(RecruitingApplication.class);
        Post base = mock(Post.class);
        User user = mock(User.class);
        ApplicationSubmission submission = mock(ApplicationSubmission.class);
        Department dept = mock(Department.class);

        given(postRepo.findByIdAndDeletedAtIsNull(1L)).willReturn(Optional.of(post));
        given(post.getPost()).willReturn(base);
        given(base.getAuthor()).willReturn(user);
        given(user.getId()).willReturn(1L);
        given(appRepo.findByRecruitingPost(post)).willReturn(Optional.of(app));

        given(submission.getId()).willReturn(1L);
        given(submission.getApplicantName()).willReturn("홍길동");
        given(submission.getCampus()).willReturn(Campus.SEOUL);
        given(submission.getDepartment()).willReturn(dept);
        given(dept.getName()).willReturn("컴공");
        given(submission.getSubmittedAt()).willReturn(Instant.now());

        given(repo.findAll(
                ArgumentMatchers.<Specification<ApplicationSubmission>>any(),
                Mockito.any(Pageable.class)
        )).willReturn(new PageImpl<ApplicationSubmission>(List.of(submission)));

        Page<ApplicationSubmissionSummaryResponse> res =
                service.getSubmissionList(1L, user, null, null, null,
                        ApplicationSubmissionSortType.LATEST, PageRequest.of(0, 10));

        assertThat(res.getContent())
                .singleElement()
                .satisfies(r -> {
                    assertThat(r.applicantName()).isEqualTo("홍길동");
                    assertThat(r.department()).isEqualTo("컴공");
                });
    }
}
