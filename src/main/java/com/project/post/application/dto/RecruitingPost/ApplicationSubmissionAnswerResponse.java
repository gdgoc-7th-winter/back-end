package com.project.post.application.dto.RecruitingPost;

import com.project.post.domain.enums.RecruitingQuestionType;

import java.util.List;

public record ApplicationSubmissionAnswerResponse(
        Long questionId,
        String questionContent,
        RecruitingQuestionType questionType,
        boolean required,
        int sortOrder,
        String answer,
        List<Long> selectedOptionIds
) {
}
