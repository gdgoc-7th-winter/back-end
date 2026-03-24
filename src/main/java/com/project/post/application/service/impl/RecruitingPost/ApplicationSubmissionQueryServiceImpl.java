package com.project.post.application.service.impl.RecruitingPost;

import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.post.application.dto.RecruitingPost.ApplicationSubmissionAnswerResponse;
import com.project.post.application.dto.RecruitingPost.ApplicationSubmissionDetailResponse;
import com.project.post.application.service.ApplicationSubmissionQueryService;
import com.project.post.domain.entity.AnswerSelectedOption;
import com.project.post.domain.entity.ApplicationSubmission;
import com.project.post.domain.entity.RecruitingApplicationAnswer;
import com.project.post.domain.repository.AnswerSelectedOptionRepository;
import com.project.post.domain.repository.ApplicationSubmissionRepository;
import com.project.post.domain.repository.RecruitingApplicationAnswerRepository;
import com.project.user.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApplicationSubmissionQueryServiceImpl implements ApplicationSubmissionQueryService {

    private final ApplicationSubmissionRepository applicationSubmissionRepository;
    private final RecruitingApplicationAnswerRepository recruitingApplicationAnswerRepository;
    private final AnswerSelectedOptionRepository answerSelectedOptionRepository;

    @Override
    public ApplicationSubmissionDetailResponse getDetail(@NonNull Long submissionId,
                                                         @NonNull User user) {

        ApplicationSubmission submission = applicationSubmissionRepository.findById(submissionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "지원 내역을 찾을 수 없습니다."));

        if (!submission.getUser().getId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED, "본인이 작성한 지원서만 조회할 수 있습니다.");
        }

        List<RecruitingApplicationAnswer> answers =
                recruitingApplicationAnswerRepository.findAllByApplicationSubmissionId(submissionId);

        List<ApplicationSubmissionAnswerResponse> answerResponses = answers.stream()
                .map(answer -> {
                    List<Long> selectedOptionIds =
                            answerSelectedOptionRepository.findAllByRecruitingApplicationAnswerId(answer.getId())
                                    .stream()
                                    .map(selectedOption -> selectedOption.getQuestionOption().getId())
                                    .toList();

                    return new ApplicationSubmissionAnswerResponse(
                            answer.getQuestion().getId(),
                            answer.getQuestion().getContent(),
                            answer.getQuestion().getType(),
                            answer.getQuestion().isRequired(),
                            answer.getQuestion().getSortOrder(),
                            answer.getAnswer(),
                            selectedOptionIds
                    );
                })
                .toList();

        return new ApplicationSubmissionDetailResponse(
                submission.getId(),
                submission.getRecruitingApplication().getRecruitingPost().getId(),
                submission.getSubmittedAt(),
                answerResponses
        );
    }
}