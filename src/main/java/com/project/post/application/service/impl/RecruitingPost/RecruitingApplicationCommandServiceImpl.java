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
import java.util.*;
import java.util.stream.Collectors;

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

        RecruitingPost recruitingPost = recruitingPostRepository.findByIdForUpdate(postId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.RESOURCE_NOT_FOUND,
                        "리크루팅 게시글을 찾을 수 없습니다."
                ));

        RecruitingApplication recruitingApplication = recruitingApplicationRepository.findByRecruitingPostForUpdate(recruitingPost)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.RESOURCE_NOT_FOUND,
                        "지원폼을 찾을 수 없습니다."
                ));

        if (recruitingPost.getDeletedAt() != null) {
            throw new BusinessException(
                    ErrorCode.RESOURCE_NOT_FOUND,
                    "삭제된 모집글입니다."
            );
        }

        if (!recruitingPost.isOpenForApplication()) {
            throw new BusinessException(ErrorCode.APPLICATION_NOT_AVAILABLE);
        }

        if (applicationSubmissionRepository.existsByRecruitingApplicationAndUserAndDeletedAtIsNull(recruitingApplication, user)) {
            throw new BusinessException(ErrorCode.ALREADY_APPLIED);
        }

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.RESOURCE_NOT_FOUND,
                        "존재하지 않는 학과입니다."
                ));

        validateRequiredAnswers(recruitingApplication, request.getAnswers());

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

        Map<Long, RecruitingQuestion> questionMap = getQuestionMap(
                recruitingApplication,
                request.getAnswers()
        );

        Map<Long, RecruitingQuestionOption> optionMap = getOptionMap(request.getAnswers());

        List<RecruitingApplicationAnswer> answersToSave = new ArrayList<>();
        List<AnswerSelectedOption> selectedOptionsToSave = new ArrayList<>();

        for (AnswerRequest answerRequest : request.getAnswers()) {
            RecruitingQuestion question = questionMap.get(answerRequest.getQuestionId());
            if (question == null) {
                throw new BusinessException(
                        ErrorCode.RESOURCE_NOT_FOUND,
                        "질문을 찾을 수 없습니다."
                );
            }

            RecruitingApplicationAnswer answer = RecruitingApplicationAnswer.builder()
                    .applicationSubmission(savedSubmission)
                    .question(question)
                    .answer(answerRequest.getAnswer())
                    .build();

            RecruitingApplicationAnswer savedAnswer = recruitingApplicationAnswerRepository.save(answer);
            answersToSave.add(savedAnswer);

            if (answerRequest.getSelectedOptionIds() == null || answerRequest.getSelectedOptionIds().isEmpty()) {
                continue;
            }

            for (Long optionId : answerRequest.getSelectedOptionIds()) {
                RecruitingQuestionOption option = optionMap.get(optionId);
                if (option == null) {
                    throw new BusinessException(
                            ErrorCode.RESOURCE_NOT_FOUND,
                            "선택지를 찾을 수 없습니다."
                    );
                }

                if (!option.getQuestion().getId().equals(question.getId())) {
                    throw new BusinessException(ErrorCode.INVALID_OPTION);
                }

                selectedOptionsToSave.add(new AnswerSelectedOption(savedAnswer, option));
            }
        }

        if (!selectedOptionsToSave.isEmpty()) {
            answerSelectedOptionRepository.saveAll(selectedOptionsToSave);
        }

        return savedSubmission.getId();
    }

    @Override
    @Transactional
    public void updateSubmission(@NonNull Long submissionId,
                                 @NonNull ApplicationSubmissionUpdateRequest request,
                                 @NonNull User user) {

        ApplicationSubmission submission = applicationSubmissionRepository.findByIdAndDeletedAtIsNull(submissionId)
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

        if (recruitingPost.getDeletedAt() != null) {
            throw new BusinessException(ErrorCode.SUBMISSION_UPDATE_NOT_ALLOWED);
        }

        if (!recruitingPost.isOpenForApplication()) {
            throw new BusinessException(ErrorCode.SUBMISSION_UPDATE_NOT_ALLOWED);
        }

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.RESOURCE_NOT_FOUND,
                        "존재하지 않는 학과입니다."
                ));

        validateRequiredAnswers(recruitingApplication, request.getAnswers());

        submission.updateApplicantInfo(
                request.getApplicantName(),
                request.getCampus(),
                department
        );

        int deletedSelectedOptionsCount =
                answerSelectedOptionRepository.deleteByApplicationSubmissionId(submissionId);

        recruitingApplicationAnswerRepository.deleteAllByApplicationSubmissionId(submissionId);

        if (request.getAnswers() == null || request.getAnswers().isEmpty()) {
            return;
        }

        Map<Long, RecruitingQuestion> questionMap = getQuestionMap(
                recruitingApplication,
                request.getAnswers()
        );

        Map<Long, RecruitingQuestionOption> optionMap = getOptionMap(request.getAnswers());

        List<AnswerSelectedOption> selectedOptionsToSave = new ArrayList<>();

        for (AnswerRequest answerRequest : request.getAnswers()) {
            RecruitingQuestion question = questionMap.get(answerRequest.getQuestionId());
            if (question == null) {
                throw new BusinessException(
                        ErrorCode.RESOURCE_NOT_FOUND,
                        "질문을 찾을 수 없습니다."
                );
            }

            RecruitingApplicationAnswer answer = RecruitingApplicationAnswer.builder()
                    .applicationSubmission(submission)
                    .question(question)
                    .answer(answerRequest.getAnswer())
                    .build();

            RecruitingApplicationAnswer savedAnswer = recruitingApplicationAnswerRepository.save(answer);

            if (answerRequest.getSelectedOptionIds() == null || answerRequest.getSelectedOptionIds().isEmpty()) {
                continue;
            }

            for (Long optionId : answerRequest.getSelectedOptionIds()) {
                RecruitingQuestionOption option = optionMap.get(optionId);
                if (option == null) {
                    throw new BusinessException(
                            ErrorCode.RESOURCE_NOT_FOUND,
                            "선택지를 찾을 수 없습니다."
                    );
                }

                if (!option.getQuestion().getId().equals(question.getId())) {
                    throw new BusinessException(ErrorCode.INVALID_OPTION);
                }

                selectedOptionsToSave.add(new AnswerSelectedOption(savedAnswer, option));
            }
        }

        if (!selectedOptionsToSave.isEmpty()) {
            answerSelectedOptionRepository.saveAll(selectedOptionsToSave);
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

    private void validateRequiredAnswers(RecruitingApplication recruitingApplication,
                                         List<AnswerRequest> answerRequests) {

        List<RecruitingQuestion> questions =
                recruitingQuestionRepository.findAllByRecruitingApplicationIdOrderBySortOrderAsc(
                        recruitingApplication.getId()
                );

        Map<Long, AnswerRequest> answerRequestMap = new HashMap<>();
        if (answerRequests != null) {
            for (AnswerRequest answerRequest : answerRequests) {
                answerRequestMap.put(answerRequest.getQuestionId(), answerRequest);
            }
        }

        for (RecruitingQuestion question : questions) {
            AnswerRequest answerRequest = answerRequestMap.get(question.getId());

            if (!question.isRequired()) {
                continue;
            }

            if (answerRequest == null) {
                throw new BusinessException(ErrorCode.MISSING_REQUIRED_ANSWER);
            }

            boolean hasTextAnswer =
                    answerRequest.getAnswer() != null && !answerRequest.getAnswer().trim().isEmpty();

            boolean hasSelectedOptions =
                    answerRequest.getSelectedOptionIds() != null && !answerRequest.getSelectedOptionIds().isEmpty();

            // 주관식 필수인데 텍스트 없음
            // 객관식 필수인데 선택지 없음
            // 둘 다 비어 있으면 무조건 실패
            if (!hasTextAnswer && !hasSelectedOptions) {
                throw new BusinessException(ErrorCode.MISSING_REQUIRED_ANSWER);
            }
        }
    }

    private Map<Long, RecruitingQuestion> getQuestionMap(
            RecruitingApplication recruitingApplication,
            List<AnswerRequest> answerRequests
    ) {
        Set<Long> questionIds = answerRequests.stream()
                .map(AnswerRequest::getQuestionId)
                .collect(Collectors.toSet());

        if (questionIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<RecruitingQuestion> questions = recruitingQuestionRepository.findAllById(questionIds);

        Map<Long, RecruitingQuestion> questionMap = questions.stream()
                .collect(Collectors.toMap(RecruitingQuestion::getId, question -> question));

        for (Long questionId : questionIds) {
            RecruitingQuestion question = questionMap.get(questionId);

            if (question == null) {
                throw new BusinessException(
                        ErrorCode.RESOURCE_NOT_FOUND,
                        "질문을 찾을 수 없습니다."
                );
            }

            if (!question.getRecruitingApplication().getId().equals(recruitingApplication.getId())) {
                throw new BusinessException(ErrorCode.INVALID_QUESTION);
            }
        }

        return questionMap;
    }

    private Map<Long, RecruitingQuestionOption> getOptionMap(List<AnswerRequest> answerRequests) {
        Set<Long> optionIds = answerRequests.stream()
                .flatMap(answerRequest -> {
                    List<Long> selectedOptionIds = answerRequest.getSelectedOptionIds();
                    return selectedOptionIds == null ? java.util.stream.Stream.empty() : selectedOptionIds.stream();
                })
                .collect(Collectors.toSet());

        if (optionIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<RecruitingQuestionOption> options = recruitingQuestionOptionRepository.findAllById(optionIds);

        Map<Long, RecruitingQuestionOption> optionMap = options.stream()
                .collect(Collectors.toMap(RecruitingQuestionOption::getId, option -> option));

        for (Long optionId : optionIds) {
            if (!optionMap.containsKey(optionId)) {
                throw new BusinessException(
                        ErrorCode.RESOURCE_NOT_FOUND,
                        "선택지를 찾을 수 없습니다."
                );
            }
        }

        return optionMap;
    }
}
