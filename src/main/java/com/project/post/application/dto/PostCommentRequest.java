package com.project.post.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record PostCommentRequest(
        @NotBlank(message = "댓글 내용은 필수입니다.")
        @Size(max = 10_000, message = "댓글은 10,000자를 초과할 수 없습니다.")
        String content,
        @Positive Long parentCommentId
) {
}
