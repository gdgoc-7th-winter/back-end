package com.project.post.application.service.impl.RecruitingPost;

import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.post.application.dto.RecruitingPost.ApplicationSubmissionAnswerResponse;
import com.project.post.application.dto.RecruitingPost.ApplicationSubmissionDetailResponse;
import com.project.post.application.dto.RecruitingPost.ApplicationSubmissionListResponse;
import com.project.post.application.dto.RecruitingPost.ApplicationSubmissionSummaryResponse;
import com.project.post.application.service.ApplicationSubmissionQueryService;
import com.project.post.domain.entity.*;
import com.project.post.domain.repository.*;
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
    private final RecruitingPostRepository recruitingPostRepository;
    private final RecruitingApplicationRepository recruitingApplicationRepository;

    @Override
    public ApplicationSubmissionDetailResponse getDetail(@NonNull Long submissionId,
                                                         @NonNull User user) {

        ApplicationSubmission submission = applicationSubmissionRepository.findByIdAndDeletedAtIsNull(submissionId)
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
                submission.getApplicantName(),
                submission.getCampus(),
                submission.getDepartment(),
                submission.getSubmittedAt(),
                answerResponses
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApplicationSubmissionListResponse getSubmissionList(Long postId, User user) {
        RecruitingPost recruitingPost = recruitingPostRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "리크루팅 게시글을 찾을 수 없습니다."));

        if (!recruitingPost.getPost().getAuthor().getId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED, "해당 모집글의 지원서는 작성자만 조회할 수 있습니다.");
        }

        RecruitingApplication recruitingApplication = recruitingApplicationRepository.findByRecruitingPost(recruitingPost)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "지원폼을 찾을 수 없습니다."));

        List<ApplicationSubmissionSummaryResponse> submissions =
                applicationSubmissionRepository.findAllByRecruitingApplicationAndDeletedAtIsNullOrderBySubmittedAtDesc(recruitingApplication)
                        .stream()
                        .map(ApplicationSubmissionSummaryResponse::from)
                        .toList();

        return new ApplicationSubmissionListResponse(submissions);
    }
}