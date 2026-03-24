package com.project.post.application.dto.RecruitingPost;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class ApplicationSubmissionUpdateRequest {

    @Valid
    @NotEmpty(message = "답변은 비어 있을 수 없습니다.")
    private List<AnswerRequest> answers;
}