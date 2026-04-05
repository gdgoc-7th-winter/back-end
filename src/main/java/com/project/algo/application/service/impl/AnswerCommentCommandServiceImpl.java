package com.project.algo.application.service.impl;

import com.project.algo.application.dto.AnswerCommentCreateRequest;
import com.project.algo.application.dto.AnswerCommentUpdateRequest;
import com.project.algo.application.service.AnswerCommentCommandService;
import com.project.algo.domain.entity.AnswerCodePost;
import com.project.algo.domain.entity.AnswerComment;
import com.project.algo.domain.repository.AnswerCodePostRepository;
import com.project.algo.domain.repository.AnswerCommentRepository;
import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.user.domain.entity.User;
import com.project.user.domain.enums.Authority;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AnswerCommentCommandServiceImpl implements AnswerCommentCommandService {

    private final AnswerCommentRepository answerCommentRepository;
    private final AnswerCodePostRepository answerCodePostRepository;

    @Override
    @Transactional
    public Long create(@NonNull Long answerId, @NonNull AnswerCommentCreateRequest request, @NonNull User author) {
        AnswerCodePost answerCodePost = answerCodePostRepository.findById(answerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "풀이를 찾을 수 없습니다."));

        AnswerComment comment = AnswerComment.builder()
                .answerCodePost(answerCodePost)
                .author(author)
                .content(request.content())
                .commentTag(request.commentTag())
                .build();

        if (request.referencedLines() != null) {
            comment.getReferencedLines().addAll(request.referencedLines());
        }

        return answerCommentRepository.save(comment).getId();
    }

    @Override
    @Transactional
    public void update(@NonNull Long commentId, @NonNull AnswerCommentUpdateRequest request, @NonNull User author) {
        AnswerComment comment = answerCommentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "코멘트를 찾을 수 없습니다."));

        if (!comment.getAuthor().getId().equals(author.getId())) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED, "수정 권한이 없습니다.");
        }

        comment.update(request.content(), request.commentTag());
    }

    @Override
    @Transactional
    public void delete(@NonNull Long commentId, @NonNull User author) {
        AnswerComment comment = answerCommentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "코멘트를 찾을 수 없습니다."));

        if (!comment.getAuthor().getId().equals(author.getId())
                && author.getAuthority() != Authority.ADMIN) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED, "삭제 권한이 없습니다.");
        }

        comment.softDelete();
    }
}
