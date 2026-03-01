package com.project.post.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PostUpdateRequest(
        @Size(max = 200)
        String title,
        @Size(max = 50_000, message = "본문은 50,000자를 초과할 수 없습니다.")
        String content,
        String thumbnailUrl,
        @Size(max = 20, message = "태그는 최대 20개까지 등록 가능합니다.")
        List<String> tagNames,
        @Valid List<PostAttachmentRequest> attachments
) {
}
