package com.project.post.application.service.impl.LecturePost;

import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.post.application.dto.LecturePost.LecturePostCreateRequest;
import com.project.post.application.dto.LecturePost.LecturePostUpdateRequest;
import com.project.post.application.service.LecturePostCommandService;
import com.project.post.application.service.PostAttachmentService;
import com.project.post.application.service.PostTagService;
import com.project.post.domain.entity.Board;
import com.project.post.domain.entity.LecturePost;
import com.project.post.domain.entity.Post;
import com.project.post.domain.exception.PostDomainException;
import com.project.post.domain.repository.BoardRepository;
import com.project.post.domain.repository.LecturePostRepository;
import com.project.post.domain.repository.PostRepository;
import com.project.user.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class LecturePostCommandServiceImpl implements LecturePostCommandService {

    private static final String BOARD_CODE = "LECTURE";

    private final BoardRepository boardRepository;
    private final PostRepository postRepository;
    private final LecturePostRepository lecturePostRepository;
    private final PostTagService postTagService;
    private final PostAttachmentService postAttachmentService;

    @Override
    @Transactional
    public Long create(@NonNull LecturePostCreateRequest request, @NonNull User author) {
        Board board = boardRepository.findByCodeAndActiveTrue(BOARD_CODE)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "강의/수업 게시판을 찾을 수 없습니다."));

        Post post = Post.builder()
                .board(board)
                .author(author)
                .title(request.title())
                .content(request.content())
                .thumbnailUrl(request.thumbnailUrl())
                .build();

        Post savedPost = postRepository.save(Objects.requireNonNull(post));

        LecturePost lecturePost = LecturePost.builder()
                .post(savedPost)
                .department(request.department())
                .campus(request.campus())
                .build();

        lecturePostRepository.save(lecturePost);

        postTagService.replaceTags(Objects.requireNonNull(savedPost), request.tagNames());
        postAttachmentService.replaceAttachments(Objects.requireNonNull(savedPost), request.attachments());

        postRepository.flush();

        return Objects.requireNonNull(savedPost.getId());
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

        try {
            post.update(request.title(), request.content(), request.thumbnailUrl());
        } catch (PostDomainException ex) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, ex.getMessage());
        }

        lecturePost.update(request.department(), request.campus());

        postTagService.replaceTags(post, request.tagNames());
        postAttachmentService.replaceAttachments(post, request.attachments());

        postRepository.flush();
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
        post.softDelete();
    }
}
