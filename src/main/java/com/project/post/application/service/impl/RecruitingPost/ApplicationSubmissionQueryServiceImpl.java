package com.project.post.application.service.impl.RecruitingPost;

import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.post.application.dto.PostAuthorResponse;
import com.project.post.application.dto.PostListResponse;
import com.project.post.application.dto.PostViewerResponse;
import com.project.post.application.dto.RecruitingPost.ApplicationSubmissionAnswerResponse;
import com.project.post.application.dto.RecruitingPost.ApplicationSubmissionDetailResponse;
import com.project.post.application.dto.RecruitingPost.ApplicationSubmissionSummaryResponse;
import com.project.post.application.dto.RecruitingPost.AppliedRecruitingPostListResponse;
import com.project.post.application.dto.RecruitingPost.AppliedRecruitingPostSummaryResponse;
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
import com.project.post.domain.repository.dto.AppliedRecruitingPostListQueryResult;
import com.project.post.domain.specification.ApplicationSubmissionSpecification;
import com.project.user.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApplicationSubmissionQueryServiceImpl implements ApplicationSubmissionQueryService {

    private static final int MAX_PAGE_SIZE = 100;

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

        List<Long> answerIds = answers.stream()
                .map(RecruitingApplicationAnswer::getId)
                .toList();

        Map<Long, List<Long>> selectedOptionIdsMap = answerSelectedOptionRepository
                .findAllByRecruitingApplicationAnswerIdIn(answerIds)
                .stream()
                .collect(Collectors.groupingBy(
                        selectedOption -> selectedOption.getRecruitingApplicationAnswer().getId(),
                        Collectors.mapping(
                                selectedOption -> selectedOption.getQuestionOption().getId(),
                                Collectors.toList()
                        )
                ));

        List<ApplicationSubmissionAnswerResponse> answerResponses = answers.stream()
                .map(answer -> new ApplicationSubmissionAnswerResponse(
                        answer.getQuestion().getId(),
                        answer.getQuestion().getContent(),
                        answer.getQuestion().getType(),
                        answer.getQuestion().isRequired(),
                        answer.getQuestion().getSortOrder(),
                        answer.getAnswer(),
                        selectedOptionIdsMap.getOrDefault(answer.getId(), List.of())
                ))
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
    public Page<ApplicationSubmissionSummaryResponse> getSubmissionList(
            @NonNull Long postId,
            @NonNull User user,
            Campus campus,
            Long departmentId,
            String applicantName,
            String sort,
            Pageable pageable
    ) {
        RecruitingPost recruitingPost = recruitingPostRepository.findByIdAndDeletedAtIsNull(postId)
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

        int safeSize = Math.min(pageable.getPageSize(), MAX_PAGE_SIZE);

        Pageable safePageable = PageRequest.of(
                pageable.getPageNumber(),
                safeSize,
                getSortCondition(sort)
        );

        Specification<ApplicationSubmission> spec =
                ApplicationSubmissionSpecification.hasRecruitingApplication(recruitingApplication)
                        .and(ApplicationSubmissionSpecification.isNotDeleted())
                        .and(ApplicationSubmissionSpecification.hasCampus(campus))
                        .and(ApplicationSubmissionSpecification.hasDepartmentId(departmentId))
                        .and(ApplicationSubmissionSpecification.applicantNameContains(applicantName));

        Page<ApplicationSubmission> submissionPage =
                applicationSubmissionRepository.findAll(spec, safePageable);

        return submissionPage.map(ApplicationSubmissionSummaryResponse::from);
    }

    @Override
    public AppliedRecruitingPostListResponse getAppliedRecruitings(@NonNull User user) {

        List<AppliedRecruitingPostListQueryResult> results =
                applicationSubmissionRepository.findAppliedRecruitingPostListByUserId(user.getId());

        List<AppliedRecruitingPostSummaryResponse> recruitings = results.stream()
                .map(result -> {
                    RecruitingStatus status = calculateStatus(result.startedAt(), result.deadlineAt());

                    return new AppliedRecruitingPostSummaryResponse(
                            result.submissionId(),
                            result.category(),
                            status,
                            calculateStatusLabel(result.startedAt(), result.deadlineAt()),
                            result.startedAt(),
                            result.deadlineAt(),
                            result.submittedAt(),
                            new PostListResponse(
                                    result.postId(),
                                    result.title(),
                                    result.thumbnailUrl(),
                                    PostAuthorResponse.fromParts(
                                            result.authorId(),
                                            result.authorNickname(),
                                            result.authorProfileImgUrl(),
                                            result.authorDepartmentName(),
                                            result.authorRepresentativeTrackName(),
                                            result.authorLevelImageUrl(),
                                            result.authorIsWithdrawn()
                                    ),
                                    result.viewCount(),
                                    result.likeCount(),
                                    result.scrapCount(),
                                    result.commentCount(),
                                    PostViewerResponse.guest(),
                                    List.of(),
                                    result.createdAt()
                            )
                    );
                })
                .toList();

        return new AppliedRecruitingPostListResponse(recruitings);
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