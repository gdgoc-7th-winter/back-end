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

@Service
@RequiredArgsConstructor
public class RecruitingPostCommandServiceImpl implements RecruitingPostCommandService {

    private static final String RECRUITING_BOARD_CODE = "RECRUITING";

    private final PostCommandService postCommandService;
    private final RecruitingPostRepository recruitingPostRepository;

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

        return post.getId();
    }
}