package com.project.algo.application.dto;

import com.project.algo.domain.enums.CommentTag;
import jakarta.validation.constraints.Size;

public record AnswerCommentUpdateRequest(

        @Size(max = 5_000)
        String content,

        CommentTag commentTag
) {}
