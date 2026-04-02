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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

        RecruitingPost recruitingPost = getRecruitingPostForSubmit(postId);
        RecruitingApplication recruitingApplication = getRecruitingApplicationForSubmit(recruitingPost);

        validateSubmitAvailable(recruitingPost, recruitingApplication, user);

        Department department = getDepartment(request.getDepartmentId());
        validateRequiredAnswers(recruitingApplication, request.getAnswers());

        ApplicationSubmission savedSubmission = createAndSaveSubmission(
                recruitingApplication,
                request,
                user,
                department
        );

        saveAnswers(savedSubmission, recruitingApplication, request.getAnswers());

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

        validateSubmissionOwner(submission, user);

        RecruitingApplication recruitingApplication = submission.getRecruitingApplication();
        RecruitingPost recruitingPost = recruitingApplication.getRecruitingPost();

        validateSubmissionUpdateAvailable(recruitingPost);

        Department department = getDepartment(request.getDepartmentId());
        validateRequiredAnswers(recruitingApplication, request.getAnswers());

        submission.updateApplicantInfo(
                request.getApplicantName(),
                request.getCampus(),
                department
        );

        clearExistingAnswers(submissionId);
        saveAnswers(submission, recruitingApplication, request.getAnswers());
    }

    @Override
    @Transactional
    public void cancelSubmission(@NonNull Long submissionId, @NonNull User user) {
        ApplicationSubmission submission = applicationSubmissionRepository.findByIdAndDeletedAtIsNull(submissionId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.RESOURCE_NOT_FOUND,
                        "지원 내역을 찾을 수 없습니다."
                ));

        validateSubmissionOwner(submission, user);

        RecruitingPost recruitingPost = submission.getRecruitingApplication().getRecruitingPost();

        if (recruitingPost.getDeletedAt() != null) {
            throw new BusinessException(ErrorCode.SUBMISSION_CANCEL_NOT_ALLOWED);
        }

        if (!recruitingPost.isOpenForApplication()) {
            throw new BusinessException(ErrorCode.SUBMISSION_CANCEL_NOT_ALLOWED);
        }

        submission.softDelete();
    }

    private RecruitingPost getRecruitingPostForSubmit(Long postId) {
        return recruitingPostRepository.findByIdForUpdate(postId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.RESOURCE_NOT_FOUND,
                        "리크루팅 게시글을 찾을 수 없습니다."
                ));
    }

    private RecruitingApplication getRecruitingApplicationForSubmit(RecruitingPost recruitingPost) {
        return recruitingApplicationRepository.findByRecruitingPostForUpdate(recruitingPost)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.RESOURCE_NOT_FOUND,
                        "지원폼을 찾을 수 없습니다."
                ));
    }

    private void validateSubmitAvailable(RecruitingPost recruitingPost,
                                         RecruitingApplication recruitingApplication,
                                         User user) {
        if (recruitingPost.getDeletedAt() != null) {
            throw new BusinessException(
                    ErrorCode.RESOURCE_NOT_FOUND,
                    "삭제된 모집글입니다."
            );
        }

        if (!recruitingPost.isOpenForApplication()) {
            throw new BusinessException(ErrorCode.APPLICATION_NOT_AVAILABLE);
        }

        if (applicationSubmissionRepository.existsByRecruitingApplicationAndUserAndDeletedAtIsNull(
                recruitingApplication,
                user
        )) {
            throw new BusinessException(ErrorCode.ALREADY_APPLIED);
        }
    }

    private void validateSubmissionOwner(ApplicationSubmission submission, User user) {
        if (!submission.getUser().getId().equals(user.getId())) {
            throw new BusinessException(
                    ErrorCode.ACCESS_DENIED,
                    "본인이 작성한 지원서만 수정할 수 있습니다."
            );
        }
    }

    private void validateSubmissionUpdateAvailable(RecruitingPost recruitingPost) {
        if (recruitingPost.getDeletedAt() != null) {
            throw new BusinessException(ErrorCode.SUBMISSION_UPDATE_NOT_ALLOWED);
        }

        if (!recruitingPost.isOpenForApplication()) {
            throw new BusinessException(ErrorCode.SUBMISSION_UPDATE_NOT_ALLOWED);
        }
    }

    private Department getDepartment(Long departmentId) {
        return departmentRepository.findById(departmentId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.RESOURCE_NOT_FOUND,
                        "존재하지 않는 학과입니다."
                ));
    }

    private ApplicationSubmission createAndSaveSubmission(RecruitingApplication recruitingApplication,
                                                          SubmitApplicationRequest request,
                                                          User user,
                                                          Department department) {
        ApplicationSubmission submission = ApplicationSubmission.builder()
                .recruitingApplication(recruitingApplication)
                .user(user)
                .submittedAt(Instant.now())
                .applicantName(request.getApplicantName())
                .campus(request.getCampus())
                .department(department)
                .build();

        try {
            return applicationSubmissionRepository.save(submission);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.ALREADY_APPLIED);
        }
    }

    private void clearExistingAnswers(Long submissionId) {
        answerSelectedOptionRepository.deleteByApplicationSubmissionId(submissionId);
        recruitingApplicationAnswerRepository.deleteAllByApplicationSubmissionId(submissionId);
    }

    private void saveAnswers(ApplicationSubmission submission,
                             RecruitingApplication recruitingApplication,
                             List<AnswerRequest> answerRequests) {
        if (answerRequests == null || answerRequests.isEmpty()) {
            return;
        }

        Map<Long, RecruitingQuestion> questionMap = getQuestionMap(recruitingApplication, answerRequests);
        Map<Long, RecruitingQuestionOption> optionMap = getOptionMap(answerRequests);

        List<AnswerSelectedOption> selectedOptionsToSave = new ArrayList<>();

        for (AnswerRequest answerRequest : answerRequests) {
            RecruitingQuestion question = getValidatedQuestion(questionMap, answerRequest.getQuestionId());
            RecruitingApplicationAnswer savedAnswer = saveApplicationAnswer(submission, question, answerRequest);

            appendSelectedOptions(
                    selectedOptionsToSave,
                    savedAnswer,
                    question,
                    answerRequest,
                    optionMap
            );
        }

        if (!selectedOptionsToSave.isEmpty()) {
            answerSelectedOptionRepository.saveAll(selectedOptionsToSave);
        }
    }

    private RecruitingQuestion getValidatedQuestion(Map<Long, RecruitingQuestion> questionMap, Long questionId) {
        RecruitingQuestion question = questionMap.get(questionId);

        if (question == null) {
            throw new BusinessException(
                    ErrorCode.RESOURCE_NOT_FOUND,
                    "질문을 찾을 수 없습니다."
            );
        }

        return question;
    }

    private RecruitingApplicationAnswer saveApplicationAnswer(ApplicationSubmission submission,
                                                              RecruitingQuestion question,
                                                              AnswerRequest answerRequest) {
        RecruitingApplicationAnswer answer = RecruitingApplicationAnswer.builder()
                .applicationSubmission(submission)
                .question(question)
                .answer(answerRequest.getAnswer())
                .build();

        return recruitingApplicationAnswerRepository.save(answer);
    }

    private void appendSelectedOptions(List<AnswerSelectedOption> selectedOptionsToSave,
                                       RecruitingApplicationAnswer savedAnswer,
                                       RecruitingQuestion question,
                                       AnswerRequest answerRequest,
                                       Map<Long, RecruitingQuestionOption> optionMap) {
        if (answerRequest.getSelectedOptionIds() == null || answerRequest.getSelectedOptionIds().isEmpty()) {
            return;
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
                .collect(Collectors.toMap(RecruitingQuestion::getId, question -> question, (a, b) -> a, LinkedHashMap::new));

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
                .collect(Collectors.toMap(
                        RecruitingQuestionOption::getId,
                        option -> option,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

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