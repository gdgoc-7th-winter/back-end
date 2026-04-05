package com.project.algo.application.dto;

import java.util.Optional;

public record AlgoLikeToggleResponse(boolean liked, Optional<Integer> likeCount) {}
