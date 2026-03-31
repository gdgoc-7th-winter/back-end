package com.project.post.application.service.impl.RecruitingPost;

import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.post.application.dto.RecruitingPost.AnswerRequest;
import com.project.post.application.dto.RecruitingPost.ApplicationSubmissionUpdateRequest;
import com.project.post.application.dto.RecruitingPost.SubmitApplicationRequest;
import com.project.post.application.service.RecruitingApplicationCommandService;
import com.project.post.domain.entity.AnswerSelectedOption;
import com.project.post.domain.entity.ApplicationSubmission;
import com.project.post.domain.entity.RecruitingApplication;
import com.project.post.domain.entity.RecruitingApplicationAnswer;
import com.project.post.domain.entity.RecruitingPost;
import com.project.post.domain.entity.RecruitingQuestion;
import com.project.post.domain.entity.RecruitingQuestionOption;
import com.project.post.domain.repository.AnswerSelectedOptionRepository;
import com.project.post.domain.repository.ApplicationSubmissionRepository;
import com.project.post.domain.repository.RecruitingApplicationAnswerRepository;
import com.project.post.domain.repository.RecruitingApplicationRepository;
import com.project.post.domain.repository.RecruitingPostRepository;
import com.project.post.domain.repository.RecruitingQuestionOptionRepository;
import com.project.post.domain.repository.RecruitingQuestionRepository;
import com.project.user.domain.entity.Department;
import com.project.user.domain.entity.User;
import com.project.user.domain.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class RecruitingApplicationCommandServiceImpl implements RecruitingApplicationCommandService {

    private final RecruitingPostRepository recruitingPostRepository;
    private final RecruitingApplicationRepository recruitingApplicationRepository;
    private final RecruitingQuestionRepository recruitingQuestionRepository;
    private final RecruitingQuestionOptionRepository recruitingQuestionOptionRepository;
    private final ApplicationSubmissionRepository applicationSubmissionRepository;
    private final RecruitingApplicationAnswerRepository recruitingApplicationAnswerRepository;
    private final AnswerSelectedOptionRepository answerSelectedOptionRepository;
    private final DepartmentRepository departmentRepository;

    @Override
    @Transactional
    public Long submit(@NonNull Long postId,
                       @NonNull SubmitApplicationRequest request,
                       @NonNull User user) {

        RecruitingPost recruitingPost = recruitingPostRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.RESOURCE_NOT_FOUND,
                        "리크루팅 게시글을 찾을 수 없습니다."
                ));

        RecruitingApplication recruitingApplication = recruitingApplicationRepository.findByRecruitingPost(recruitingPost)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.RESOURCE_NOT_FOUND,
                        "지원폼을 찾을 수 없습니다."
                ));

        if (applicationSubmissionRepository.existsByRecruitingApplicationAndUserAndDeletedAtIsNull(recruitingApplication, user)) {
            throw new BusinessException(ErrorCode.ALREADY_APPLIED);
        }

        if (!recruitingPost.isOpenForApplication()) {
            throw new BusinessException(ErrorCode.APPLICATION_NOT_AVAILABLE);
        }

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.RESOURCE_NOT_FOUND,
                        "존재하지 않는 학과입니다."
                ));

        ApplicationSubmission submission = ApplicationSubmission.builder()
                .recruitingApplication(recruitingApplication)
                .user(user)
                .submittedAt(Instant.now())
                .applicantName(request.getApplicantName())
                .campus(request.getCampus())
                .department(department)
                .build();

        ApplicationSubmission savedSubmission;
        try {
            savedSubmission = applicationSubmissionRepository.save(submission);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.ALREADY_APPLIED);
        }

        if (request.getAnswers() == null || request.getAnswers().isEmpty()) {
            return savedSubmission.getId();
        }

        for (AnswerRequest answerRequest : request.getAnswers()) {
            RecruitingQuestion question = recruitingQuestionRepository.findById(answerRequest.getQuestionId())
                    .orElseThrow(() -> new BusinessException(
                            ErrorCode.RESOURCE_NOT_FOUND,
                            "질문을 찾을 수 없습니다."
                    ));

            if (!question.getRecruitingApplication().getId().equals(recruitingApplication.getId())) {
                throw new BusinessException(ErrorCode.INVALID_QUESTION);
            }

            RecruitingApplicationAnswer savedAnswer = RecruitingApplicationAnswer.builder()
                    .applicationSubmission(savedSubmission)
                    .question(question)
                    .answer(answerRequest.getAnswer())
                    .build();

            recruitingApplicationAnswerRepository.save(savedAnswer);

            if (answerRequest.getSelectedOptionIds() == null || answerRequest.getSelectedOptionIds().isEmpty()) {
                continue;
            }

            for (Long optionId : answerRequest.getSelectedOptionIds()) {
                RecruitingQuestionOption option = recruitingQuestionOptionRepository.findById(optionId)
                        .orElseThrow(() -> new BusinessException(
                                ErrorCode.RESOURCE_NOT_FOUND,
                                "선택지를 찾을 수 없습니다."
                        ));

                if (!option.getQuestion().getId().equals(question.getId())) {
                    throw new BusinessException(ErrorCode.INVALID_OPTION);
                }

                answerSelectedOptionRepository.save(
                        new AnswerSelectedOption(savedAnswer, option)
                );
            }
        }

        return savedSubmission.getId();
    }

    @Override
    @Transactional
    public void updateSubmission(@NonNull Long submissionId,
                                 @NonNull ApplicationSubmissionUpdateRequest request,
                                 @NonNull User user) {

        ApplicationSubmission submission = applicationSubmissionRepository.findById(submissionId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.RESOURCE_NOT_FOUND,
                        "지원 내역을 찾을 수 없습니다."
                ));

        if (!submission.getUser().getId().equals(user.getId())) {
            throw new BusinessException(
                    ErrorCode.ACCESS_DENIED,
                    "본인이 작성한 지원서만 수정할 수 있습니다."
            );
        }

        RecruitingApplication recruitingApplication = submission.getRecruitingApplication();
        RecruitingPost recruitingPost = recruitingApplication.getRecruitingPost();

        if (!recruitingPost.isOpenForApplication()) {
            throw new BusinessException(ErrorCode.SUBMISSION_UPDATE_NOT_ALLOWED);
        }

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.RESOURCE_NOT_FOUND,
                        "존재하지 않는 학과입니다."
                ));

        submission.updateApplicantInfo(
                request.getApplicantName(),
                request.getCampus(),
                department
        );

        answerSelectedOptionRepository.deleteAllByRecruitingApplicationAnswerApplicationSubmissionId(submissionId);
        recruitingApplicationAnswerRepository.deleteAllByApplicationSubmissionId(submissionId);

        if (request.getAnswers() == null || request.getAnswers().isEmpty()) {
            return;
        }

        for (AnswerRequest answerRequest : request.getAnswers()) {
            RecruitingQuestion question = recruitingQuestionRepository.findById(answerRequest.getQuestionId())
                    .orElseThrow(() -> new BusinessException(
                            ErrorCode.RESOURCE_NOT_FOUND,
                            "질문을 찾을 수 없습니다."
                    ));

            if (!question.getRecruitingApplication().getId().equals(recruitingApplication.getId())) {
                throw new BusinessException(ErrorCode.INVALID_QUESTION);
            }

            RecruitingApplicationAnswer savedAnswer = RecruitingApplicationAnswer.builder()
                    .applicationSubmission(submission)
                    .question(question)
                    .answer(answerRequest.getAnswer())
                    .build();

            recruitingApplicationAnswerRepository.save(savedAnswer);

            if (answerRequest.getSelectedOptionIds() == null || answerRequest.getSelectedOptionIds().isEmpty()) {
                continue;
            }

            for (Long optionId : answerRequest.getSelectedOptionIds()) {
                RecruitingQuestionOption option = recruitingQuestionOptionRepository.findById(optionId)
                        .orElseThrow(() -> new BusinessException(
                                ErrorCode.RESOURCE_NOT_FOUND,
                                "선택지를 찾을 수 없습니다."
                        ));

                if (!option.getQuestion().getId().equals(question.getId())) {
                    throw new BusinessException(ErrorCode.INVALID_OPTION);
                }

                answerSelectedOptionRepository.save(
                        new AnswerSelectedOption(savedAnswer, option)
                );
            }
        }
    }

    @Override
    @Transactional
    public void cancelSubmission(@NonNull Long submissionId, @NonNull User user) {
        ApplicationSubmission submission = applicationSubmissionRepository.findByIdAndDeletedAtIsNull(submissionId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.RESOURCE_NOT_FOUND,
                        "지원 내역을 찾을 수 없습니다."
                ));

        if (!submission.getUser().getId().equals(user.getId())) {
            throw new BusinessException(
                    ErrorCode.ACCESS_DENIED,
                    "본인이 작성한 지원서만 취소할 수 있습니다."
            );
        }

        RecruitingPost recruitingPost = submission.getRecruitingApplication().getRecruitingPost();

        if (!recruitingPost.isOpenForApplication()) {
            throw new BusinessException(ErrorCode.SUBMISSION_CANCEL_NOT_ALLOWED);
        }

        submission.softDelete();
    }
}
