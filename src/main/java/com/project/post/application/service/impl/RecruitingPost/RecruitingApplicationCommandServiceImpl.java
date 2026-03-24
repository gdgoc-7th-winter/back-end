package com.project.post.application.service.impl.RecruitingPost;

import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.post.application.dto.RecruitingPost.AnswerRequest;
import com.project.post.application.dto.RecruitingPost.SubmitApplicationRequest;
import com.project.post.application.dto.RecruitingPost.ApplicationSubmissionUpdateRequest;
import com.project.post.application.service.RecruitingApplicationCommandService;
import com.project.post.domain.entity.*;
import com.project.post.domain.repository.*;
import com.project.user.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Override
    @Transactional
    public void submit(@NonNull SubmitApplicationRequest request,
                       @NonNull User user) {

        RecruitingPost recruitingPost = recruitingPostRepository.findById(request.getRecruitingPostId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "리크루팅 게시글을 찾을 수 없습니다."));

        RecruitingApplication recruitingApplication = recruitingApplicationRepository.findByRecruitingPost(recruitingPost)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "지원폼을 찾을 수 없습니다."));

        if (recruitingPost.getDeadlineAt() != null && Instant.now().isAfter(recruitingPost.getDeadlineAt())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "모집 마감 후에는 지원할 수 없습니다.");
        }

        ApplicationSubmission submission = ApplicationSubmission.builder()
                .recruitingApplication(recruitingApplication)
                .user(user)
                .submittedAt(Instant.now())
                .build();

        applicationSubmissionRepository.save(submission);

        if (request.getAnswers() == null || request.getAnswers().isEmpty()) {
            return;
        }

        for (AnswerRequest answerRequest : request.getAnswers()) {
            RecruitingQuestion question = recruitingQuestionRepository.findById(answerRequest.getQuestionId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "질문을 찾을 수 없습니다."));

            if (!question.getRecruitingApplication().getId().equals(recruitingApplication.getId())) {
                throw new BusinessException(ErrorCode.INVALID_INPUT, "해당 지원폼의 질문만 답변할 수 있습니다.");
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
                        .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "선택지를 찾을 수 없습니다."));

                if (!option.getQuestion().getId().equals(question.getId())) {
                    throw new BusinessException(ErrorCode.INVALID_INPUT, "해당 질문에 속하지 않은 선택지입니다.");
                }

                answerSelectedOptionRepository.save(
                        new AnswerSelectedOption(savedAnswer, option)
                );
            }
        }
    }

    @Override
    @Transactional
    public void updateSubmission(@NonNull Long submissionId,
                                 @NonNull ApplicationSubmissionUpdateRequest request,
                                 @NonNull User user) {

        ApplicationSubmission submission = applicationSubmissionRepository.findById(submissionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "지원 내역을 찾을 수 없습니다."));

        if (!submission.getUser().getId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED, "본인이 작성한 지원서만 수정할 수 있습니다.");
        }

        RecruitingApplication recruitingApplication = submission.getRecruitingApplication();
        RecruitingPost recruitingPost = recruitingApplication.getRecruitingPost();

        if (recruitingPost.getDeadlineAt() != null && Instant.now().isAfter(recruitingPost.getDeadlineAt())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "모집 마감 후에는 지원 답변을 수정할 수 없습니다.");
        }

        answerSelectedOptionRepository.deleteAllByRecruitingApplicationAnswerApplicationSubmissionId(submissionId);
        recruitingApplicationAnswerRepository.deleteAllByApplicationSubmissionId(submissionId);

        if (request.getAnswers() == null || request.getAnswers().isEmpty()) {
            return;
        }

        for (AnswerRequest answerRequest : request.getAnswers()) {
            RecruitingQuestion question = recruitingQuestionRepository.findById(answerRequest.getQuestionId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "질문을 찾을 수 없습니다."));

            if (!question.getRecruitingApplication().getId().equals(recruitingApplication.getId())) {
                throw new BusinessException(ErrorCode.INVALID_INPUT, "해당 지원폼의 질문만 수정할 수 있습니다.");
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
                        .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "선택지를 찾을 수 없습니다."));

                if (!option.getQuestion().getId().equals(question.getId())) {
                    throw new BusinessException(ErrorCode.INVALID_INPUT, "해당 질문에 속하지 않은 선택지입니다.");
                }

                answerSelectedOptionRepository.save(new AnswerSelectedOption(savedAnswer, option));
            }
        }
    }
}