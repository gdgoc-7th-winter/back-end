package com.project.algo.application.service;

import com.project.algo.application.dto.DailyChallengeCreateRequest;
import com.project.algo.application.dto.DailyChallengeUpdateRequest;
import com.project.user.domain.entity.User;

public interface DailyChallengeCommandService {

    Long create(DailyChallengeCreateRequest request, User author);

    void update(Long challengeId, DailyChallengeUpdateRequest request, User author);

    void delete(Long challengeId, User author);
}
