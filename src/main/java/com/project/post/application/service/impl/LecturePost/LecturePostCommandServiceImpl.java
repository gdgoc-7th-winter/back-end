package com.project.post.application.service.impl.LecturePost;

import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.post.application.dto.LecturePost.LecturePostCreateRequest;
import com.project.post.application.dto.LecturePost.LecturePostUpdateRequest;
import com.project.post.application.dto.PostCreateRequest;
import com.project.post.application.dto.PostUpdateRequest;
import com.project.post.application.service.LecturePostCommandService;
import com.project.post.application.service.PostCommandService;
import com.project.post.domain.entity.LecturePost;
import com.project.post.domain.entity.Post;
import com.project.post.domain.repository.LecturePostRepository;
import com.project.post.domain.repository.PostRepository;
import com.project.user.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LecturePostCommandServiceImpl implements LecturePostCommandService {

    private static final String BOARD_CODE = "LECTURE";

    private final PostCommandService postCommandService;
    private final PostRepository postRepository;
    private final LecturePostRepository lecturePostRepository;

    @Override
    @Transactional
    public Long create(@NonNull LecturePostCreateRequest request, @NonNull User author) {
        PostCreateRequest postCreateRequest = new PostCreateRequest(
                request.title(),
                request.content(),
                request.thumbnailUrl(),
                request.tagNames(),
                request.attachments()
        );

        Post post = postCommandService.create(BOARD_CODE, postCreateRequest, author);
        Post postRef = postRepository.getReferenceById(post.getId());

        LecturePost lecturePost = LecturePost.builder()
                .post(postRef)
                .department(request.department())
                .campus(request.campus())
                .build();

        lecturePostRepository.save(lecturePost);

        return post.getId();
    }

    @Override
    @Transactional
    public void update(@NonNull Long postId, @NonNull LecturePostUpdateRequest request, @NonNull User author) {
        LecturePost lecturePost = lecturePostRepository.findActiveById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "강의/수업 게시글을 찾을 수 없습니다."));

        Post post = lecturePost.getPost();

        if (!post.getAuthor().getId().equals(author.getId())) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED, "수정 권한이 없습니다.");
        }

        PostUpdateRequest postUpdateRequest = new PostUpdateRequest(
                request.title(),
                request.content(),
                request.thumbnailUrl(),
                request.tagNames(),
                request.attachments()
        );
        postCommandService.update(postId, postUpdateRequest, author);

        lecturePost.update(request.department(), request.campus());
    }

    @Override
    @Transactional
    public void delete(@NonNull Long postId, @NonNull User author) {
        LecturePost lecturePost = lecturePostRepository.findActiveById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "강의/수업 게시글을 찾을 수 없습니다."));

        Post post = lecturePost.getPost();

        if (!post.getAuthor().getId().equals(author.getId())) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED, "삭제 권한이 없습니다.");
        }

        lecturePost.softDelete();
        postCommandService.softDelete(post.getId(), author);
    }
}
