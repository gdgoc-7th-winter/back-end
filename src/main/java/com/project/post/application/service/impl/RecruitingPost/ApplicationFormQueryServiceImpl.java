package com.project.post.application.service.impl.RecruitingPost;

import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.post.application.dto.RecruitingPost.ApplicationFormDetailResponse;
import com.project.post.application.dto.RecruitingPost.ApplicationFormQuestionResponse;
import com.project.post.application.service.ApplicationFormQueryService;
import com.project.post.domain.entity.RecruitingApplication;
import com.project.post.domain.entity.RecruitingPost;
import com.project.post.domain.repository.RecruitingApplicationRepository;
import com.project.post.domain.repository.RecruitingPostRepository;
import com.project.post.domain.repository.RecruitingQuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApplicationFormQueryServiceImpl implements ApplicationFormQueryService {

    private final RecruitingPostRepository recruitingPostRepository;
    private final RecruitingApplicationRepository recruitingApplicationRepository;
    private final RecruitingQuestionRepository recruitingQuestionRepository;

    @Override
    public ApplicationFormDetailResponse getApplicationFormDetail(Long recruitingPostId) {
        RecruitingPost recruitingPost = recruitingPostRepository.findById(recruitingPostId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        RecruitingApplication recruitingApplication = recruitingApplicationRepository.findByRecruitingPost(recruitingPost)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        List<ApplicationFormQuestionResponse> questions = recruitingQuestionRepository
                .findAllByRecruitingApplicationIdOrderBySortOrderAsc(recruitingApplication.getId())
                .stream()
                .map(ApplicationFormQuestionResponse::from)
                .toList();

        return ApplicationFormDetailResponse.of(recruitingPost, recruitingApplication, questions);
    }
}