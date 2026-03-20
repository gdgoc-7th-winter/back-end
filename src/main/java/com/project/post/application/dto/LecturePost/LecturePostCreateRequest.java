package com.project.post.application.dto.LecturePost;

import com.project.post.application.dto.PostAttachmentRequest;
import com.project.post.domain.enums.Campus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record LecturePostCreateRequest(

        @NotBlank(message = "제목은 필수입니다.")
        @Size(max = 200)
        String title,

        @NotBlank(message = "본문은 필수입니다.")
        @Size(max = 50_000, message = "본문은 50,000자를 초과할 수 없습니다.")
        String content,

        String thumbnailUrl,

        @NotBlank(message = "학과는 필수입니다.")
        @Size(max = 100, message = "학과명은 100자를 초과할 수 없습니다.")
        String department,

        @NotNull(message = "캠퍼스는 필수입니다.")
        Campus campus,

        @Size(max = 20, message = "태그는 최대 20개까지 등록 가능합니다.")
        List<@Size(max = 50, message = "태그는 50자를 초과할 수 없습니다.") String> tagNames,

        @Size(max = 10, message = "첨부파일은 최대 10개까지 등록 가능합니다.")
        @Valid
        List<PostAttachmentRequest> attachments
) {
}
