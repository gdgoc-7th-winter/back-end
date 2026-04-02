package com.project.post.application.service;

import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.post.application.dto.RecruitingPost.AnswerRequest;
import com.project.post.application.dto.RecruitingPost.ApplicationSubmissionUpdateRequest;
import com.project.post.application.dto.RecruitingPost.SubmitApplicationRequest;
import com.project.post.application.service.impl.RecruitingPost.RecruitingApplicationCommandServiceImpl;
import com.project.post.domain.entity.ApplicationSubmission;
import com.project.user.domain.entity.Department;
import com.project.post.domain.entity.RecruitingApplication;
import com.project.post.domain.entity.RecruitingApplicationAnswer;
import com.project.post.domain.entity.RecruitingPost;
import com.project.post.domain.entity.RecruitingQuestion;
import com.project.post.domain.entity.RecruitingQuestionOption;
import com.project.user.domain.entity.User;
import com.project.post.domain.repository.AnswerSelectedOptionRepository;
import com.project.post.domain.repository.ApplicationSubmissionRepository;
import com.project.post.domain.repository.RecruitingApplicationAnswerRepository;
import com.project.post.domain.repository.RecruitingApplicationRepository;
import com.project.post.domain.repository.RecruitingPostRepository;
import com.project.post.domain.repository.RecruitingQuestionOptionRepository;
import com.project.post.domain.repository.RecruitingQuestionRepository;
import com.project.user.domain.repository.DepartmentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import static org.mockito.BDDMockito.given;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.anySet;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class RecruitingApplicationCommandServiceImplTest {

    @Mock
    private RecruitingPostRepository recruitingPostRepository;
    @Mock
    private RecruitingApplicationRepository recruitingApplicationRepository;
    @Mock
    private RecruitingQuestionRepository recruitingQuestionRepository;
    @Mock
    private RecruitingQuestionOptionRepository recruitingQuestionOptionRepository;
    @Mock
    private ApplicationSubmissionRepository applicationSubmissionRepository;
    @Mock
    private RecruitingApplicationAnswerRepository recruitingApplicationAnswerRepository;
    @Mock
    private AnswerSelectedOptionRepository answerSelectedOptionRepository;
    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private RecruitingApplicationCommandServiceImpl service;

    @Nested
    class SubmitTest {

        @Test
        @DisplayName("submit: 정상 요청이면 지원서와 답변을 저장한다")
        void submitSuccess() {
            Long postId = 1L;
            Long userId = 10L;
            Long departmentId = 100L;
            Long questionId = 1000L;
            Long optionId = 2000L;
            Long submissionId = 3000L;

            RecruitingPost recruitingPost = mock(RecruitingPost.class);
            RecruitingApplication recruitingApplication = mock(RecruitingApplication.class);
            RecruitingQuestion question = mock(RecruitingQuestion.class);
            RecruitingQuestionOption option = mock(RecruitingQuestionOption.class);
            Department department = mock(Department.class);
            User user = mock(User.class);

            SubmitApplicationRequest request = mock(SubmitApplicationRequest.class);
            AnswerRequest answerRequest = mock(AnswerRequest.class);

            ApplicationSubmission savedSubmission = mock(ApplicationSubmission.class);
            RecruitingApplicationAnswer savedAnswer = mock(RecruitingApplicationAnswer.class);

            given(recruitingPostRepository.findByIdForUpdate(postId))
                    .willReturn(Optional.of(recruitingPost));
            given(recruitingApplicationRepository.findByRecruitingPostForUpdate(recruitingPost))
                    .willReturn(Optional.of(recruitingApplication));

            given(recruitingPost.getDeletedAt()).willReturn(null);
            given(recruitingPost.isOpenForApplication()).willReturn(true);
            given(applicationSubmissionRepository.existsByRecruitingApplicationAndUserAndDeletedAtIsNull(recruitingApplication, user))
                    .willReturn(false);

            given(request.getDepartmentId()).willReturn(departmentId);
            given(request.getApplicantName()).willReturn("홍길동");
            given(request.getCampus()).willReturn(null);
            given(request.getAnswers()).willReturn(List.of(answerRequest));

            given(departmentRepository.findById(departmentId))
                    .willReturn(Optional.of(department));

            given(answerRequest.getQuestionId()).willReturn(questionId);
            given(answerRequest.getAnswer()).willReturn("백엔드 지원합니다");
            given(answerRequest.getSelectedOptionIds()).willReturn(List.of(optionId));

            given(recruitingQuestionRepository.findAllByRecruitingApplicationIdOrderBySortOrderAsc(anyLong()))
                    .willReturn(List.of(question));
            given(recruitingApplication.getId()).willReturn(999L);
            given(question.getId()).willReturn(questionId);
            given(question.isRequired()).willReturn(true);
            given(question.getRecruitingApplication()).willReturn(recruitingApplication);

            given(recruitingQuestionRepository.findAllById(anySet()))
                    .willReturn(List.of(question));

            given(recruitingQuestionOptionRepository.findAllById(anySet()))
                    .willReturn(List.of(option));
            given(option.getId()).willReturn(optionId);
            given(option.getQuestion()).willReturn(question);

            given(applicationSubmissionRepository.save(any(ApplicationSubmission.class)))
                    .willReturn(savedSubmission);
            given(savedSubmission.getId()).willReturn(submissionId);

            given(recruitingApplicationAnswerRepository.save(any(RecruitingApplicationAnswer.class)))
                    .willReturn(savedAnswer);

            Long result = service.submit(postId, request, user);

            assertEquals(submissionId, result);
            verify(applicationSubmissionRepository).save(any(ApplicationSubmission.class));
            verify(recruitingApplicationAnswerRepository).save(any(RecruitingApplicationAnswer.class));
            verify(answerSelectedOptionRepository).saveAll(anyList());
        }

        @Test
        @DisplayName("submit: 이미 지원한 경우 ALREADY_APPLIED 예외가 발생한다")
        void submitAlreadyApplied() {
            Long postId = 1L;

            RecruitingPost recruitingPost = mock(RecruitingPost.class);
            RecruitingApplication recruitingApplication = mock(RecruitingApplication.class);
            SubmitApplicationRequest request = mock(SubmitApplicationRequest.class);
            User user = mock(User.class);

            given(recruitingPostRepository.findByIdForUpdate(postId))
                    .willReturn(Optional.of(recruitingPost));
            given(recruitingApplicationRepository.findByRecruitingPostForUpdate(recruitingPost))
                    .willReturn(Optional.of(recruitingApplication));

            given(recruitingPost.getDeletedAt()).willReturn(null);
            given(recruitingPost.isOpenForApplication()).willReturn(true);
            given(applicationSubmissionRepository.existsByRecruitingApplicationAndUserAndDeletedAtIsNull(recruitingApplication, user))
                    .willReturn(true);

            BusinessException ex = assertThrows(
                    BusinessException.class,
                    () -> service.submit(postId, request, user)
            );

            assertBusinessError(ex, ErrorCode.ALREADY_APPLIED);
            verify(applicationSubmissionRepository, never()).save(any());
        }

        @Test
        @DisplayName("submit: 필수 질문 답변이 없으면 MISSING_REQUIRED_ANSWER 예외가 발생한다")
        void submitMissingRequiredAnswer() {
            Long postId = 1L;
            Long departmentId = 100L;
            Long questionId = 1000L;

            RecruitingPost recruitingPost = mock(RecruitingPost.class);
            RecruitingApplication recruitingApplication = mock(RecruitingApplication.class);
            RecruitingQuestion question = mock(RecruitingQuestion.class);
            Department department = mock(Department.class);
            User user = mock(User.class);

            SubmitApplicationRequest request = mock(SubmitApplicationRequest.class);
            AnswerRequest answerRequest = mock(AnswerRequest.class);

            given(recruitingPostRepository.findByIdForUpdate(postId))
                    .willReturn(Optional.of(recruitingPost));
            given(recruitingApplicationRepository.findByRecruitingPostForUpdate(recruitingPost))
                    .willReturn(Optional.of(recruitingApplication));

            given(recruitingPost.getDeletedAt()).willReturn(null);
            given(recruitingPost.isOpenForApplication()).willReturn(true);
            given(applicationSubmissionRepository.existsByRecruitingApplicationAndUserAndDeletedAtIsNull(recruitingApplication, user))
                    .willReturn(false);

            given(request.getDepartmentId()).willReturn(departmentId);
            given(request.getAnswers()).willReturn(List.of(answerRequest));
            given(departmentRepository.findById(departmentId))
                    .willReturn(Optional.of(department));

            given(recruitingApplication.getId()).willReturn(999L);
            given(recruitingQuestionRepository.findAllByRecruitingApplicationIdOrderBySortOrderAsc(999L))
                    .willReturn(List.of(question));

            given(question.getId()).willReturn(questionId);
            given(question.isRequired()).willReturn(true);

            given(answerRequest.getQuestionId()).willReturn(questionId);
            given(answerRequest.getAnswer()).willReturn("   ");
            given(answerRequest.getSelectedOptionIds()).willReturn(List.of());

            BusinessException ex = assertThrows(
                    BusinessException.class,
                    () -> service.submit(postId, request, user)
            );

            assertBusinessError(ex, ErrorCode.MISSING_REQUIRED_ANSWER);
            verify(applicationSubmissionRepository, never()).save(any());
        }

        @Test
        @DisplayName("submit: 질문에 속하지 않는 선택지를 선택하면 INVALID_OPTION 예외가 발생한다")
        void submitInvalidOptionThrowsException() {
            Long postId = 1L;
            Long departmentId = 100L;
            Long questionId = 1000L;
            Long anotherQuestionId = 1001L;
            Long optionId = 2000L;

            RecruitingPost recruitingPost = mock(RecruitingPost.class);
            RecruitingApplication recruitingApplication = mock(RecruitingApplication.class);
            RecruitingQuestion question = mock(RecruitingQuestion.class);
            RecruitingQuestion anotherQuestion = mock(RecruitingQuestion.class);
            RecruitingQuestionOption option = mock(RecruitingQuestionOption.class);
            Department department = mock(Department.class);
            User user = mock(User.class);

            SubmitApplicationRequest request = mock(SubmitApplicationRequest.class);
            AnswerRequest answerRequest = mock(AnswerRequest.class);

            ApplicationSubmission savedSubmission = mock(ApplicationSubmission.class);
            RecruitingApplicationAnswer savedAnswer = mock(RecruitingApplicationAnswer.class);

            given(recruitingPostRepository.findByIdForUpdate(postId))
                    .willReturn(Optional.of(recruitingPost));
            given(recruitingApplicationRepository.findByRecruitingPostForUpdate(recruitingPost))
                    .willReturn(Optional.of(recruitingApplication));

            given(recruitingPost.getDeletedAt()).willReturn(null);
            given(recruitingPost.isOpenForApplication()).willReturn(true);
            given(applicationSubmissionRepository.existsByRecruitingApplicationAndUserAndDeletedAtIsNull(recruitingApplication, user))
                    .willReturn(false);

            given(request.getDepartmentId()).willReturn(departmentId);
            given(request.getApplicantName()).willReturn("홍길동");
            given(request.getCampus()).willReturn(null);
            given(request.getAnswers()).willReturn(List.of(answerRequest));

            given(departmentRepository.findById(departmentId))
                    .willReturn(Optional.of(department));

            given(recruitingApplication.getId()).willReturn(999L);
            given(recruitingQuestionRepository.findAllByRecruitingApplicationIdOrderBySortOrderAsc(999L))
                    .willReturn(List.of(question));

            given(question.getId()).willReturn(questionId);
            given(question.isRequired()).willReturn(true);
            given(question.getRecruitingApplication()).willReturn(recruitingApplication);

            given(answerRequest.getQuestionId()).willReturn(questionId);
            given(answerRequest.getAnswer()).willReturn("답변");
            given(answerRequest.getSelectedOptionIds()).willReturn(List.of(optionId));

            given(recruitingQuestionRepository.findAllById(anySet()))
                    .willReturn(List.of(question));

            given(option.getId()).willReturn(optionId);
            given(option.getQuestion()).willReturn(anotherQuestion);
            given(anotherQuestion.getId()).willReturn(anotherQuestionId);

            given(recruitingQuestionOptionRepository.findAllById(anySet()))
                    .willReturn(List.of(option));

            given(applicationSubmissionRepository.save(any(ApplicationSubmission.class)))
                    .willReturn(savedSubmission);
            given(recruitingApplicationAnswerRepository.save(any(RecruitingApplicationAnswer.class)))
                    .willReturn(savedAnswer);

            BusinessException ex = assertThrows(
                    BusinessException.class,
                    () -> service.submit(postId, request, user)
            );

            assertBusinessError(ex, ErrorCode.INVALID_OPTION);
            verify(answerSelectedOptionRepository, never()).saveAll(anyList());
        }

        @Test
        @DisplayName("submit: 존재하지 않는 질문이면 RESOURCE_NOT_FOUND 예외가 발생한다")
        void submitMissingQuestionThrowsException() {
            Long postId = 1L;
            Long departmentId = 100L;
            Long questionId = 1000L;

            RecruitingPost recruitingPost = mock(RecruitingPost.class);
            RecruitingApplication recruitingApplication = mock(RecruitingApplication.class);
            RecruitingQuestion requiredQuestion = mock(RecruitingQuestion.class);
            Department department = mock(Department.class);
            User user = mock(User.class);

            SubmitApplicationRequest request = mock(SubmitApplicationRequest.class);
            AnswerRequest answerRequest = mock(AnswerRequest.class);

            given(recruitingPostRepository.findByIdForUpdate(postId))
                    .willReturn(Optional.of(recruitingPost));
            given(recruitingApplicationRepository.findByRecruitingPostForUpdate(recruitingPost))
                    .willReturn(Optional.of(recruitingApplication));

            given(recruitingPost.getDeletedAt()).willReturn(null);
            given(recruitingPost.isOpenForApplication()).willReturn(true);
            given(applicationSubmissionRepository.existsByRecruitingApplicationAndUserAndDeletedAtIsNull(recruitingApplication, user))
                    .willReturn(false);

            given(request.getDepartmentId()).willReturn(departmentId);
            given(request.getApplicantName()).willReturn("홍길동");
            given(request.getCampus()).willReturn(null);
            given(request.getAnswers()).willReturn(List.of(answerRequest));

            given(departmentRepository.findById(departmentId))
                    .willReturn(Optional.of(department));

            given(recruitingApplication.getId()).willReturn(999L);
            given(recruitingQuestionRepository.findAllByRecruitingApplicationIdOrderBySortOrderAsc(999L))
                    .willReturn(List.of(requiredQuestion));

            given(requiredQuestion.getId()).willReturn(questionId);
            given(requiredQuestion.isRequired()).willReturn(true);

            given(answerRequest.getQuestionId()).willReturn(questionId);
            given(answerRequest.getAnswer()).willReturn("답변");
            given(answerRequest.getSelectedOptionIds()).willReturn(List.of());

            given(applicationSubmissionRepository.save(any(ApplicationSubmission.class)))
                    .willReturn(mock(ApplicationSubmission.class));

            given(recruitingQuestionRepository.findAllById(anySet()))
                    .willReturn(List.of());

            BusinessException ex = assertThrows(
                    BusinessException.class,
                    () -> service.submit(postId, request, user)
            );

            assertBusinessError(ex, ErrorCode.RESOURCE_NOT_FOUND);
        }

        @Test
        @DisplayName("submit: 존재하지 않는 선택지이면 RESOURCE_NOT_FOUND 예외가 발생한다")
        void submitMissingOptionThrowsException() {
            Long postId = 1L;
            Long departmentId = 100L;
            Long questionId = 1000L;
            Long optionId = 2000L;

            RecruitingPost recruitingPost = mock(RecruitingPost.class);
            RecruitingApplication recruitingApplication = mock(RecruitingApplication.class);
            RecruitingQuestion question = mock(RecruitingQuestion.class);
            Department department = mock(Department.class);
            User user = mock(User.class);

            SubmitApplicationRequest request = mock(SubmitApplicationRequest.class);
            AnswerRequest answerRequest = mock(AnswerRequest.class);

            ApplicationSubmission savedSubmission = mock(ApplicationSubmission.class);
            RecruitingApplicationAnswer savedAnswer = mock(RecruitingApplicationAnswer.class);

            given(recruitingPostRepository.findByIdForUpdate(postId))
                    .willReturn(Optional.of(recruitingPost));
            given(recruitingApplicationRepository.findByRecruitingPostForUpdate(recruitingPost))
                    .willReturn(Optional.of(recruitingApplication));

            given(recruitingPost.getDeletedAt()).willReturn(null);
            given(recruitingPost.isOpenForApplication()).willReturn(true);
            given(applicationSubmissionRepository.existsByRecruitingApplicationAndUserAndDeletedAtIsNull(recruitingApplication, user))
                    .willReturn(false);

            given(request.getDepartmentId()).willReturn(departmentId);
            given(request.getApplicantName()).willReturn("홍길동");
            given(request.getCampus()).willReturn(null);
            given(request.getAnswers()).willReturn(List.of(answerRequest));

            given(departmentRepository.findById(departmentId))
                    .willReturn(Optional.of(department));

            given(recruitingApplication.getId()).willReturn(999L);
            given(recruitingQuestionRepository.findAllByRecruitingApplicationIdOrderBySortOrderAsc(999L))
                    .willReturn(List.of(question));

            given(question.getId()).willReturn(questionId);
            given(question.isRequired()).willReturn(true);
            given(question.getRecruitingApplication()).willReturn(recruitingApplication);

            given(answerRequest.getQuestionId()).willReturn(questionId);
            given(answerRequest.getAnswer()).willReturn("답변");
            given(answerRequest.getSelectedOptionIds()).willReturn(List.of(optionId));

            given(recruitingQuestionRepository.findAllById(anySet()))
                    .willReturn(List.of(question));

            given(recruitingQuestionOptionRepository.findAllById(anySet()))
                    .willReturn(List.of());

            given(applicationSubmissionRepository.save(any(ApplicationSubmission.class)))
                    .willReturn(savedSubmission);
            given(recruitingApplicationAnswerRepository.save(any(RecruitingApplicationAnswer.class)))
                    .willReturn(savedAnswer);

            BusinessException ex = assertThrows(
                    BusinessException.class,
                    () -> service.submit(postId, request, user)
            );

            assertBusinessError(ex, ErrorCode.RESOURCE_NOT_FOUND);
        }

        @Test
        @DisplayName("submit: 모집 마감 후에는 APPLICATION_NOT_AVAILABLE 예외가 발생한다")
        void submitClosedPostThrowsException() {
            Long postId = 1L;

            RecruitingPost recruitingPost = mock(RecruitingPost.class);
            RecruitingApplication recruitingApplication = mock(RecruitingApplication.class);
            SubmitApplicationRequest request = mock(SubmitApplicationRequest.class);
            User user = mock(User.class);

            given(recruitingPostRepository.findByIdForUpdate(postId))
                    .willReturn(Optional.of(recruitingPost));
            given(recruitingApplicationRepository.findByRecruitingPostForUpdate(recruitingPost))
                    .willReturn(Optional.of(recruitingApplication));

            given(recruitingPost.getDeletedAt()).willReturn(null);
            given(recruitingPost.isOpenForApplication()).willReturn(false);

            BusinessException ex = assertThrows(
                    BusinessException.class,
                    () -> service.submit(postId, request, user)
            );

            assertBusinessError(ex, ErrorCode.APPLICATION_NOT_AVAILABLE);
            verify(applicationSubmissionRepository, never()).save(any());
        }
    }

    @Nested
    class UpdateSubmissionTest {

        @Test
        @DisplayName("updateSubmission: 본인 지원서는 수정할 수 있다")
        void updateSubmissionSuccess() {
            Long submissionId = 1L;
            Long userId = 10L;
            Long departmentId = 100L;
            Long questionId = 1000L;
            Long optionId = 2000L;

            ApplicationSubmission submission = mock(ApplicationSubmission.class);
            User user = mock(User.class);
            User owner = mock(User.class);
            RecruitingApplication recruitingApplication = mock(RecruitingApplication.class);
            RecruitingPost recruitingPost = mock(RecruitingPost.class);
            RecruitingQuestion question = mock(RecruitingQuestion.class);
            RecruitingQuestionOption option = mock(RecruitingQuestionOption.class);
            Department department = mock(Department.class);

            ApplicationSubmissionUpdateRequest request = mock(ApplicationSubmissionUpdateRequest.class);
            AnswerRequest answerRequest = mock(AnswerRequest.class);
            RecruitingApplicationAnswer savedAnswer = mock(RecruitingApplicationAnswer.class);

            given(applicationSubmissionRepository.findByIdAndDeletedAtIsNull(submissionId))
                    .willReturn(Optional.of(submission));

            given(submission.getUser()).willReturn(owner);
            given(owner.getId()).willReturn(userId);
            given(user.getId()).willReturn(userId);

            given(submission.getRecruitingApplication()).willReturn(recruitingApplication);
            given(recruitingApplication.getRecruitingPost()).willReturn(recruitingPost);

            given(recruitingPost.getDeletedAt()).willReturn(null);
            given(recruitingPost.isOpenForApplication()).willReturn(true);

            given(request.getDepartmentId()).willReturn(departmentId);
            given(request.getApplicantName()).willReturn("수정된이름");
            given(request.getCampus()).willReturn(null);
            given(request.getAnswers()).willReturn(List.of(answerRequest));
            given(departmentRepository.findById(departmentId))
                    .willReturn(Optional.of(department));

            given(recruitingApplication.getId()).willReturn(999L);
            given(recruitingQuestionRepository.findAllByRecruitingApplicationIdOrderBySortOrderAsc(999L))
                    .willReturn(List.of(question));
            given(question.getId()).willReturn(questionId);
            given(question.isRequired()).willReturn(true);
            given(question.getRecruitingApplication()).willReturn(recruitingApplication);

            given(answerRequest.getQuestionId()).willReturn(questionId);
            given(answerRequest.getAnswer()).willReturn("수정 답변");
            given(answerRequest.getSelectedOptionIds()).willReturn(List.of(optionId));

            given(recruitingQuestionRepository.findAllById(anySet()))
                    .willReturn(List.of(question));

            given(recruitingQuestionOptionRepository.findAllById(anySet()))
                    .willReturn(List.of(option));
            given(option.getId()).willReturn(optionId);
            given(option.getQuestion()).willReturn(question);

            given(recruitingApplicationAnswerRepository.save(any(RecruitingApplicationAnswer.class)))
                    .willReturn(savedAnswer);

            service.updateSubmission(submissionId, request, user);

            verify(submission).updateApplicantInfo("수정된이름", null, department);
            verify(answerSelectedOptionRepository).deleteByApplicationSubmissionId(submissionId);
            verify(recruitingApplicationAnswerRepository).deleteAllByApplicationSubmissionId(submissionId);
            verify(recruitingApplicationAnswerRepository).save(any(RecruitingApplicationAnswer.class));
            verify(answerSelectedOptionRepository).saveAll(anyList());
        }

        @Test
        @DisplayName("updateSubmission: 타인 지원서는 ACCESS_DENIED 예외가 발생한다")
        void updateSubmissionAccessDenied() {
            Long submissionId = 1L;

            ApplicationSubmission submission = mock(ApplicationSubmission.class);
            User owner = mock(User.class);
            User user = mock(User.class);
            ApplicationSubmissionUpdateRequest request = mock(ApplicationSubmissionUpdateRequest.class);

            given(applicationSubmissionRepository.findByIdAndDeletedAtIsNull(submissionId))
                    .willReturn(Optional.of(submission));
            given(submission.getUser()).willReturn(owner);
            given(owner.getId()).willReturn(10L);
            given(user.getId()).willReturn(20L);

            BusinessException ex = assertThrows(
                    BusinessException.class,
                    () -> service.updateSubmission(submissionId, request, user)
            );

            assertBusinessError(ex, ErrorCode.ACCESS_DENIED);
            verify(recruitingApplicationAnswerRepository, never()).deleteAllByApplicationSubmissionId(anyLong());
        }

        @Test
        @DisplayName("updateSubmission: 모집 마감 후에는 SUBMISSION_UPDATE_NOT_ALLOWED 예외가 발생한다")
        void updateSubmissionClosedPostThrowsException() {
            Long submissionId = 1L;
            Long userId = 10L;

            ApplicationSubmission submission = mock(ApplicationSubmission.class);
            User owner = mock(User.class);
            User user = mock(User.class);
            RecruitingApplication recruitingApplication = mock(RecruitingApplication.class);
            RecruitingPost recruitingPost = mock(RecruitingPost.class);
            ApplicationSubmissionUpdateRequest request = mock(ApplicationSubmissionUpdateRequest.class);

            given(applicationSubmissionRepository.findByIdAndDeletedAtIsNull(submissionId))
                    .willReturn(Optional.of(submission));

            given(submission.getUser()).willReturn(owner);
            given(owner.getId()).willReturn(userId);
            given(user.getId()).willReturn(userId);

            given(submission.getRecruitingApplication()).willReturn(recruitingApplication);
            given(recruitingApplication.getRecruitingPost()).willReturn(recruitingPost);

            given(recruitingPost.getDeletedAt()).willReturn(null);
            given(recruitingPost.isOpenForApplication()).willReturn(false);

            BusinessException ex = assertThrows(
                    BusinessException.class,
                    () -> service.updateSubmission(submissionId, request, user)
            );

            assertBusinessError(ex, ErrorCode.SUBMISSION_UPDATE_NOT_ALLOWED);
            verify(recruitingApplicationAnswerRepository, never()).deleteAllByApplicationSubmissionId(anyLong());
        }
    }

    @Nested
    class CancelSubmissionTest {

        @Test
        @DisplayName("cancelSubmission: 본인 지원은 취소할 수 있다")
        void cancelSubmissionSuccess() {
            Long submissionId = 1L;
            Long userId = 10L;

            ApplicationSubmission submission = mock(ApplicationSubmission.class);
            User owner = mock(User.class);
            User user = mock(User.class);
            RecruitingApplication recruitingApplication = mock(RecruitingApplication.class);
            RecruitingPost recruitingPost = mock(RecruitingPost.class);

            given(applicationSubmissionRepository.findByIdAndDeletedAtIsNull(submissionId))
                    .willReturn(Optional.of(submission));
            given(submission.getUser()).willReturn(owner);
            given(owner.getId()).willReturn(userId);
            given(user.getId()).willReturn(userId);

            given(submission.getRecruitingApplication()).willReturn(recruitingApplication);
            given(recruitingApplication.getRecruitingPost()).willReturn(recruitingPost);
            given(recruitingPost.getDeletedAt()).willReturn(null);
            given(recruitingPost.isOpenForApplication()).willReturn(true);

            service.cancelSubmission(submissionId, user);

            verify(submission).softDelete();
        }

        @Test
        @DisplayName("cancelSubmission: 타인 지원은 ACCESS_DENIED 예외가 발생한다")
        void cancelSubmissionAccessDenied() {
            Long submissionId = 1L;

            ApplicationSubmission submission = mock(ApplicationSubmission.class);
            User owner = mock(User.class);
            User user = mock(User.class);

            given(applicationSubmissionRepository.findByIdAndDeletedAtIsNull(submissionId))
                    .willReturn(Optional.of(submission));
            given(submission.getUser()).willReturn(owner);
            given(owner.getId()).willReturn(10L);
            given(user.getId()).willReturn(20L);

            BusinessException ex = assertThrows(
                    BusinessException.class,
                    () -> service.cancelSubmission(submissionId, user)
            );

            assertBusinessError(ex, ErrorCode.ACCESS_DENIED);
            verify(submission, never()).softDelete();
        }

        @Test
        @DisplayName("cancelSubmission: 모집 마감 후에는 SUBMISSION_CANCEL_NOT_ALLOWED 예외가 발생한다")
        void cancelSubmissionClosedPostThrowsException() {
            Long submissionId = 1L;
            Long userId = 10L;

            ApplicationSubmission submission = mock(ApplicationSubmission.class);
            User owner = mock(User.class);
            User user = mock(User.class);
            RecruitingApplication recruitingApplication = mock(RecruitingApplication.class);
            RecruitingPost recruitingPost = mock(RecruitingPost.class);

            given(applicationSubmissionRepository.findByIdAndDeletedAtIsNull(submissionId))
                    .willReturn(Optional.of(submission));
            given(submission.getUser()).willReturn(owner);
            given(owner.getId()).willReturn(userId);
            given(user.getId()).willReturn(userId);

            given(submission.getRecruitingApplication()).willReturn(recruitingApplication);
            given(recruitingApplication.getRecruitingPost()).willReturn(recruitingPost);
            given(recruitingPost.getDeletedAt()).willReturn(null);
            given(recruitingPost.isOpenForApplication()).willReturn(false);

            BusinessException ex = assertThrows(
                    BusinessException.class,
                    () -> service.cancelSubmission(submissionId, user)
            );

            assertBusinessError(ex, ErrorCode.SUBMISSION_CANCEL_NOT_ALLOWED);
            verify(submission, never()).softDelete();
        }
    }

    private void assertBusinessError(BusinessException ex, ErrorCode expected) {
        assertNotNull(ex);
        assertEquals(expected, ex.getErrorCode());
    }
}
