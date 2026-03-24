package com.project.post.application.dto.RecruitingPost;

import com.project.post.domain.entity.RecruitingPost;
import com.project.post.domain.entity.RecruitingQuestion;
import com.project.post.domain.enums.RecruitingQuestionType;

public record ApplicationFormQuestionResponse(
        Long questionId,
        String content,
        RecruitingQuestionType questionType,
        boolean required,
        int sequence
) {
    public static ApplicationFormQuestionResponse from(RecruitingQuestion question) {
        return new ApplicationFormQuestionResponse(
                question.getId(),
                question.getContent(),
                question.getType(),
                question.isRequired(),
                question.getSortOrder()
        );
    }
}