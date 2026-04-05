package com.project.algo.application.service;

import com.project.algo.application.dto.AnswerCodePostCreateRequest;
import com.project.algo.application.dto.AnswerCodePostUpdateRequest;
import com.project.user.domain.entity.User;

public interface AnswerCodePostCommandService {

    Long create(Long challengeId, AnswerCodePostCreateRequest request, User author);

    void update(Long answerId, AnswerCodePostUpdateRequest request, User author);

    void delete(Long answerId, User author);
}
