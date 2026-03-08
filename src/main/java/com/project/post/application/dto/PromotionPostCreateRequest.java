package com.project.post.application.dto;

import com.project.post.domain.enums.PromotionCategory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PromotionPostCreateRequest(

        @NotNull(message = "카테고리는 필수입니다.")
        PromotionCategory category,

        @NotBlank(message = "제목은 필수입니다.")
        @Size(max = 100, message = "제목은 100자를 초과할 수 없습니다.")
        String title,

        @NotBlank(message = "본문은 필수입니다.")
        String content,

        String thumbnailUrl,

        @Size(max = 5, message = "태그는 최대 5개까지 등록 가능합니다.")
        List<@Size(max = 50, message = "태그는 50자를 초과할 수 없습니다.") String> tags,

        @Size(max = 10, message = "첨부파일은 최대 10개까지 등록 가능합니다.")
        @Valid List<PostAttachmentRequest> attachments

) {
}