package com.project.post.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record PostCommentRequest(
        @NotBlank(message = "댓글 내용은 필수입니다.")
        String content,
        @Positive Long parentCommentId
) {
}
