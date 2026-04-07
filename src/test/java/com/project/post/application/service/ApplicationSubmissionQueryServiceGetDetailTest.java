package com.project.post.application.service;

import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.post.application.dto.RecruitingPost.ApplicationSubmissionDetailResponse;
import com.project.post.application.service.impl.RecruitingPost.ApplicationSubmissionQueryServiceImpl;
import com.project.post.domain.entity.ApplicationSubmission;
import com.project.post.domain.entity.RecruitingApplication;
import com.project.post.domain.entity.RecruitingPost;
import com.project.post.domain.entity.Post;
import com.project.post.domain.entity.RecruitingApplicationAnswer;
import com.project.post.domain.entity.RecruitingQuestion;

import com.project.post.domain.enums.Campus;
import com.project.post.domain.enums.RecruitingQuestionType;
import com.project.post.domain.repository.ApplicationSubmissionRepository;
import com.project.post.domain.repository.RecruitingApplicationAnswerRepository;
import com.project.post.domain.repository.AnswerSelectedOptionRepository;
import com.project.post.domain.repository.RecruitingPostRepository;
import com.project.post.domain.repository.RecruitingApplicationRepository;

import com.project.user.domain.entity.Department;
import com.project.user.domain.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class ApplicationSubmissionQueryServiceGetDetailTest {

    @Mock private ApplicationSubmissionRepository applicationSubmissionRepository;
    @Mock private RecruitingApplicationAnswerRepository recruitingApplicationAnswerRepository;
    @Mock private AnswerSelectedOptionRepository answerSelectedOptionRepository;
    @Mock private RecruitingPostRepository recruitingPostRepository;
    @Mock private RecruitingApplicationRepository recruitingApplicationRepository;

    @InjectMocks
    private ApplicationSubmissionQueryServiceImpl service;

    @Test
    @DisplayName("지원자 본인은 상세 조회 가능")
    void applicantCanViewDetail() {
        Long id = 1L;

        ApplicationSubmission submission = mock(ApplicationSubmission.class);
        User user = mock(User.class);
        User applicant = mock(User.class);
        RecruitingApplication app = mock(RecruitingApplication.class);
        RecruitingPost post = mock(RecruitingPost.class);
        Post basePost = mock(Post.class);
        Department dept = mock(Department.class);
        RecruitingApplicationAnswer answer = mock(RecruitingApplicationAnswer.class);
        RecruitingQuestion question = mock(RecruitingQuestion.class);

        given(applicationSubmissionRepository.findByIdAndDeletedAtIsNull(id))
                .willReturn(Optional.of(submission));

        given(submission.getUser()).willReturn(applicant);
        given(applicant.getId()).willReturn(1L);
        given(user.getId()).willReturn(1L);

        given(submission.getRecruitingApplication()).willReturn(app);
        given(app.getRecruitingPost()).willReturn(post);
        given(post.getPost()).willReturn(basePost);

        given(submission.getId()).willReturn(id);
        given(submission.getApplicantName()).willReturn("홍길동");
        given(submission.getCampus()).willReturn(Campus.SEOUL);
        given(submission.getDepartment()).willReturn(dept);
        given(dept.getName()).willReturn("컴공");
        given(submission.getSubmittedAt()).willReturn(Instant.now());
        given(post.getId()).willReturn(100L);

        given(recruitingApplicationAnswerRepository.findAllByApplicationSubmissionId(id))
                .willReturn(List.of(answer));
        given(answerSelectedOptionRepository.findAllByRecruitingApplicationAnswerIdIn(List.of(10L)))
                .willReturn(List.of());

        given(answer.getId()).willReturn(10L);
        given(answer.getQuestion()).willReturn(question);
        given(answer.getAnswer()).willReturn("지원");

        given(question.getId()).willReturn(1L);
        given(question.getContent()).willReturn("질문");
        given(question.getType()).willReturn(RecruitingQuestionType.SHORT_TEXT);
        given(question.isRequired()).willReturn(true);
        given(question.getSortOrder()).willReturn(1);

        ApplicationSubmissionDetailResponse res = service.getDetail(id, user);

        assertThat(res.applicantName()).isEqualTo("홍길동");
        assertThat(res.answers()).hasSize(1);
    }

    @Test
    @DisplayName("권한 없으면 조회 불가")
    void throwsWhenNoPermission() {
        Long id = 1L;

        ApplicationSubmission submission = mock(ApplicationSubmission.class);
        User user = mock(User.class);
        User applicant = mock(User.class);
        User recruiter = mock(User.class);
        RecruitingApplication app = mock(RecruitingApplication.class);
        RecruitingPost post = mock(RecruitingPost.class);
        Post basePost = mock(Post.class);

        given(applicationSubmissionRepository.findByIdAndDeletedAtIsNull(id))
                .willReturn(Optional.of(submission));

        given(submission.getUser()).willReturn(applicant);
        given(applicant.getId()).willReturn(1L);
        given(user.getId()).willReturn(3L);

        given(submission.getRecruitingApplication()).willReturn(app);
        given(app.getRecruitingPost()).willReturn(post);
        given(post.getPost()).willReturn(basePost);
        given(basePost.getAuthor()).willReturn(recruiter);
        given(recruiter.getId()).willReturn(2L);

        assertThatThrownBy(() -> service.getDetail(id, user))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ACCESS_DENIED);
    }
}
