package com.project.post.application.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

public record PostAttachmentRequest(
        @NotBlank(message = "파일 URL은 필수입니다.")
        String fileUrl,
        String fileName,
        String contentType,
        @PositiveOrZero(message = "파일 크기는 0 이상이어야 합니다.")
        Long fileSize,
        @Min(value = 0, message = "정렬 순서는 0 이상이어야 합니다.")
        int sortOrder
) {
}
