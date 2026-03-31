package com.project.algo.application.dto;

import com.project.algo.domain.enums.CommentTag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record AnswerCommentCreateRequest(

        @NotBlank(message = "코멘트 내용은 필수입니다.")
        @Size(max = 5_000)
        String content,

        @Size(max = 100)
        List<Integer> referencedLines,

        CommentTag commentTag
) {}
