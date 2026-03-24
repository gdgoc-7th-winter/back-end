package com.project.post.application.service.impl.RecruitingPost;

import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.post.application.dto.RecruitingPost.RecruitingPostCreateRequest;
import com.project.post.application.service.PostCommandService;
import com.project.post.application.service.RecruitingPostCommandService;
import com.project.post.domain.entity.Post;
import com.project.post.domain.entity.RecruitingPost;
import com.project.post.domain.repository.RecruitingPostRepository;
import com.project.user.domain.entity.User;
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

@Service
@RequiredArgsConstructor
public class RecruitingPostCommandServiceImpl implements RecruitingPostCommandService {

    private static final String RECRUITING_BOARD_CODE = "RECRUITING";

    private final PostCommandService postCommandService;
    private final RecruitingPostRepository recruitingPostRepository;
    private final RecruitingApplicationRepository recruitingApplicationRepository;
    private final RecruitingQuestionRepository recruitingQuestionRepository;
    private final RecruitingQuestionOptionRepository recruitingQuestionOptionRepository;

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
}