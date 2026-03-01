package com.project.post.application.dto;

import jakarta.validation.constraints.NotBlank;

public record PostAttachmentRequest(
        @NotBlank(message = "파일 URL은 필수입니다.")
        String fileUrl,
        String fileName,
        String contentType,
        Long fileSize,
        int sortOrder
) {
}
