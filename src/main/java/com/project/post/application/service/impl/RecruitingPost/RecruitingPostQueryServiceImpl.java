package com.project.post.application.service.impl.RecruitingPost;

import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.post.application.dto.PostDetailResponse;
import com.project.post.application.dto.RecruitingPost.RecruitingPostDetailResponse;
import com.project.post.application.service.PostQueryService;
import com.project.post.application.service.RecruitingPostQueryService;
import com.project.post.domain.entity.RecruitingPost;
import com.project.post.domain.repository.RecruitingPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecruitingPostQueryServiceImpl implements RecruitingPostQueryService {

    private final RecruitingPostRepository recruitingPostRepository;
    private final PostQueryService postQueryService;

    @Override
    public RecruitingPostDetailResponse getDetail(Long postId) {

        RecruitingPost recruitingPost = recruitingPostRepository.findActiveById(postId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.RESOURCE_NOT_FOUND,
                        "모집글을 찾을 수 없습니다."
                ));

        PostDetailResponse postDetail = postQueryService.getDetail(postId);

        return new RecruitingPostDetailResponse(
                recruitingPost.getCategory(),
                recruitingPost.getApplicationType(),
                recruitingPost.getStatus(),
                recruitingPost.getStartedAt(),
                recruitingPost.getDeadlineAt(),
                postDetail
        );
    }
}
