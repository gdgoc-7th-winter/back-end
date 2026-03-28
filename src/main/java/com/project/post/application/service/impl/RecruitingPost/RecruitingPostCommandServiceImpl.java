package com.project.post.application.service.impl.RecruitingPost;

import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.post.application.dto.RecruitingPost.RecruitingPostCreateRequest;
import com.project.post.application.service.PostCommandService;
import com.project.post.application.service.RecruitingPostCommandService;
import com.project.post.domain.entity.Post;
import com.project.post.domain.entity.RecruitingPost;
import com.project.post.domain.enums.RecruitingStatus;
import com.project.post.domain.repository.RecruitingPostRepository;
import com.project.user.domain.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.post.application.dto.RecruitingPost.ApplicationFormRequest;
import com.project.post.application.dto.RecruitingPost.QuestionOptionRequest;
import com.project.post.application.dto.RecruitingPost.QuestionRequest;
import com.project.post.domain.entity.RecruitingApplication;
import com.project.post.domain.entity.RecruitingQuestion;
import com.project.post.domain.entity.RecruitingQuestionOption;
import com.project.post.domain.enums.ApplicationType;
import com.project.post.domain.repository.RecruitingApplicationRepository;
import com.project.post.domain.repository.RecruitingQuestionOptionRepository;
import com.project.post.domain.repository.RecruitingQuestionRepository;

import com.project.post.application.dto.PostUpdateRequest;
import com.project.post.application.dto.RecruitingPost.RecruitingPostDetailResponse;
import com.project.post.application.dto.RecruitingPost.RecruitingPostUpdateRequest;
import com.project.post.application.service.RecruitingPostQueryService;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class RecruitingPostCommandServiceImpl implements RecruitingPostCommandService {

    private static final String RECRUITING_BOARD_CODE = "RECRUITING";

    private final PostCommandService postCommandService;
    private final RecruitingPostRepository recruitingPostRepository;
    private final RecruitingApplicationRepository recruitingApplicationRepository;
    private final RecruitingQuestionRepository recruitingQuestionRepository;
    private final RecruitingQuestionOptionRepository recruitingQuestionOptionRepository;
    private final RecruitingPostQueryService recruitingPostQueryService;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public Long create(@NonNull RecruitingPostCreateRequest request,
                       @NonNull User user) {

        if (request.startedAt() != null && request.deadlineAt() != null
                && request.startedAt().isAfter(request.deadlineAt())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "시작일은 마감일보다 늦을 수 없습니다.");
        }

        Post post = postCommandService.create(
                RECRUITING_BOARD_CODE,
                request.post(),
                user
        );
        post = entityManager.merge(post);

        RecruitingPost recruitingPost = RecruitingPost.builder()
                .post(post)
                .category(request.category())
                .applicationType(request.applicationType())
                .startedAt(request.startedAt())
                .deadlineAt(request.deadlineAt())
                .build();

        recruitingPostRepository.save(recruitingPost);

        if (request.applicationType() == ApplicationType.FORM && request.applicationForm() != null) {
            createApplicationForm(request.applicationForm(), recruitingPost);
        }

        return post.getId();
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

    private void createApplicationForm(ApplicationFormRequest formRequest,
                                       RecruitingPost recruitingPost) {

        RecruitingApplication recruitingApplication = RecruitingApplication.builder()
                .recruitingPost(recruitingPost)
                .title(formRequest.getTitle())
                .message(formRequest.getMessage())
                .build();

        recruitingApplicationRepository.save(recruitingApplication);

        if (formRequest.getQuestions() == null || formRequest.getQuestions().isEmpty()) {
            return;
        }

        for (QuestionRequest questionRequest : formRequest.getQuestions()) {
            RecruitingQuestion question = RecruitingQuestion.builder()
                    .recruitingApplication(recruitingApplication)
                    .content(questionRequest.getContent())
                    .type(questionRequest.getType())
                    .required(questionRequest.isRequired())
                    .sortOrder(questionRequest.getSortOrder())
                    .build();

            recruitingQuestionRepository.save(question);

            if (questionRequest.getOptions() == null || questionRequest.getOptions().isEmpty()) {
                continue;
            }

            for (QuestionOptionRequest optionRequest : questionRequest.getOptions()) {
                RecruitingQuestionOption option = RecruitingQuestionOption.builder()
                        .question(question)
                        .label(optionRequest.getLabel())
                        .build();

                recruitingQuestionOptionRepository.save(option);
            }
        }
    }

    @Override
    @Transactional
    public RecruitingPostDetailResponse update(@NonNull Long postId,
                                               @NonNull RecruitingPostUpdateRequest request,
                                               @NonNull User user) {

        RecruitingPost recruitingPost = recruitingPostRepository.findByIdAndDeletedAtIsNull(postId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.RESOURCE_NOT_FOUND,
                        "모집글을 찾을 수 없습니다."
                ));

        Post post = recruitingPost.getPost();

        if (!post.getAuthor().getId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        Instant startedAt = request.startedAt() != null ? request.startedAt() : recruitingPost.getStartedAt();
        Instant deadlineAt = request.deadlineAt() != null ? request.deadlineAt() : recruitingPost.getDeadlineAt();

        if (startedAt != null && deadlineAt != null && startedAt.isAfter(deadlineAt)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        if (request.category() != null) {
            recruitingPost.updateCategory(request.category());
        }

        if (request.startedAt() != null) {
            recruitingPost.updateStartedAt(request.startedAt());
        }

        if (request.deadlineAt() != null) {
            recruitingPost.updateDeadlineAt(request.deadlineAt());
        }

        if (request.post() != null) {
            postCommandService.update(post.getId(), request.post(), user);
        }/

        return recruitingPostQueryService.getDetail(postId, user.getId());
    }

    @Override
    @Transactional
    public void delete(@NonNull Long postId, @NonNull User user) {

        RecruitingPost recruitingPost = recruitingPostRepository.findByIdAndDeletedAtIsNull(postId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.RESOURCE_NOT_FOUND,
                        "모집글을 찾을 수 없습니다."
                ));

        Post post = recruitingPost.getPost();

        if (!post.getAuthor().getId().equals(user.getId())) {
            throw new BusinessException(
                    ErrorCode.ACCESS_DENIED,
                    "본인이 작성한 모집글만 삭제할 수 있습니다."
            );
        }

        recruitingPost.softDelete();
        post.softDelete();
    }
}