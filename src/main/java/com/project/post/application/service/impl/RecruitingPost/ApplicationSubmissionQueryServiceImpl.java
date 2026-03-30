package com.project.post.application.service.impl.RecruitingPost;

import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.post.application.dto.RecruitingPost.*;
import com.project.post.application.service.ApplicationSubmissionQueryService;
import com.project.post.domain.entity.ApplicationSubmission;
import com.project.post.domain.entity.RecruitingApplication;
import com.project.post.domain.entity.RecruitingApplicationAnswer;
import com.project.post.domain.entity.RecruitingPost;
import com.project.post.domain.enums.Campus;
import com.project.post.domain.enums.RecruitingStatus;
import com.project.post.domain.repository.AnswerSelectedOptionRepository;
import com.project.post.domain.repository.ApplicationSubmissionRepository;
import com.project.post.domain.repository.RecruitingApplicationAnswerRepository;
import com.project.post.domain.repository.RecruitingApplicationRepository;
import com.project.post.domain.repository.RecruitingPostRepository;
import com.project.user.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Sort;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
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

        User applicant = submission.getUser();
        User recruiter = submission.getRecruitingApplication()
                .getRecruitingPost()
                .getPost()
                .getAuthor();

        if (!applicant.getId().equals(user.getId()) &&
                !recruiter.getId().equals(user.getId())) {

            throw new BusinessException(ErrorCode.ACCESS_DENIED, "조회 권한이 없습니다.");
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
                submission.getDepartment().getName(),
                submission.getSubmittedAt(),
                answerResponses
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApplicationSubmissionListResponse getSubmissionList(
            Long postId,
            User user,
            Campus campus,
            Long departmentId,
            String sort
    ) {
        RecruitingPost recruitingPost = recruitingPostRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.RESOURCE_NOT_FOUND,
                        "리크루팅 게시글을 찾을 수 없습니다."
                ));

        if (!recruitingPost.getPost().getAuthor().getId().equals(user.getId())) {
            throw new BusinessException(
                    ErrorCode.ACCESS_DENIED,
                    "해당 모집글의 지원서는 작성자만 조회할 수 있습니다."
            );
        }

        RecruitingApplication recruitingApplication = recruitingApplicationRepository.findByRecruitingPost(recruitingPost)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.RESOURCE_NOT_FOUND,
                        "지원폼을 찾을 수 없습니다."
                ));

        Sort sortCondition = getSortCondition(sort);

        List<ApplicationSubmission> submissionEntities;

        if (campus != null && departmentId != null) {
            submissionEntities =
                    applicationSubmissionRepository
                            .findAllByRecruitingApplicationAndCampusAndDepartment_IdAndDeletedAtIsNull(
                                    recruitingApplication,
                                    campus,
                                    departmentId,
                                    sortCondition
                            );
        } else if (campus != null) {
            submissionEntities =
                    applicationSubmissionRepository
                            .findAllByRecruitingApplicationAndCampusAndDeletedAtIsNull(
                                    recruitingApplication,
                                    campus,
                                    sortCondition
                            );
        } else if (departmentId != null) {
            submissionEntities =
                    applicationSubmissionRepository
                            .findAllByRecruitingApplicationAndDepartment_IdAndDeletedAtIsNull(
                                    recruitingApplication,
                                    departmentId,
                                    sortCondition
                            );
        } else {
            submissionEntities =
                    applicationSubmissionRepository
                            .findAllByRecruitingApplicationAndDeletedAtIsNull(
                                    recruitingApplication,
                                    sortCondition
                            );
        }

        List<ApplicationSubmissionSummaryResponse> submissions = submissionEntities.stream()
                .map(ApplicationSubmissionSummaryResponse::from)
                .toList();

        RecruitingStatus status = calculateStatus(
                recruitingPost.getStartedAt(),
                recruitingPost.getDeadlineAt()
        );

        return new ApplicationSubmissionListResponse(
                recruitingPost.getId(),
                recruitingPost.getPost().getTitle(),
                recruitingPost.getCategory(),
                status,
                calculateStatusLabel(
                        recruitingPost.getStartedAt(),
                        recruitingPost.getDeadlineAt()
                ),
                recruitingPost.getStartedAt(),
                recruitingPost.getDeadlineAt(),
                submissions.size(),
                submissions
        );
    }

    @Override
    public AppliedRecruitingPostListResponse getAppliedRecruitings(@NonNull User user) {
        return new AppliedRecruitingPostListResponse(
                applicationSubmissionRepository.findAllByUserAndDeletedAtIsNullOrderBySubmittedAtDesc(user)
                        .stream()
                        .map(AppliedRecruitingPostSummaryResponse::from)
                        .toList()
        );
    }

    private RecruitingStatus calculateStatus(Instant startedAt, Instant deadlineAt) {
        Instant now = Instant.now();

        if (startedAt != null && now.isBefore(startedAt)) {
            return RecruitingStatus.UPCOMING;
        }

        if (deadlineAt != null && now.isAfter(deadlineAt)) {
            return RecruitingStatus.CLOSED;
        }

        return RecruitingStatus.OPEN;
    }

    private String calculateStatusLabel(Instant startedAt, Instant deadlineAt) {
        Instant now = Instant.now();

        if (startedAt != null && now.isBefore(startedAt)) {
            return "모집 예정";
        }

        if (deadlineAt != null && now.isAfter(deadlineAt)) {
            return "모집 마감";
        }

        if (deadlineAt == null) {
            return "모집 중";
        }

        LocalDate today = now.atZone(ZoneId.of("Asia/Seoul")).toLocalDate();
        LocalDate deadlineDate = deadlineAt.atZone(ZoneId.of("Asia/Seoul")).toLocalDate();

        long days = ChronoUnit.DAYS.between(today, deadlineDate);

        if (days <= 0) {
            return "D-Day";
        }

        return "D-" + days;
    }

    private Sort getSortCondition(String sort) {
        if (sort == null || sort.isBlank() || "latest".equals(sort)) {
            return Sort.by(Sort.Direction.DESC, "submittedAt");
        }

        if ("name".equals(sort)) {
            return Sort.by(Sort.Direction.ASC, "applicantName")
                    .and(Sort.by(Sort.Direction.DESC, "submittedAt"));
        }

        throw new BusinessException(ErrorCode.INVALID_INPUT, "지원하지 않는 정렬 방식입니다.");
    }
}