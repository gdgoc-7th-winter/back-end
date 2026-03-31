package com.project.algo.application.service;

import com.project.algo.application.dto.AnswerCommentCreateRequest;
import com.project.algo.application.dto.AnswerCommentUpdateRequest;
import com.project.user.domain.entity.User;

public interface AnswerCommentCommandService {

    Long create(Long answerId, AnswerCommentCreateRequest request, User author);

    void update(Long commentId, AnswerCommentUpdateRequest request, User author);

    void delete(Long commentId, User author);
}
