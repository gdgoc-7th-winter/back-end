package com.project.post.application.dto.RecruitingPost;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class AnswerRequest {

    @NotNull(message = "questionId는 필수입니다.")
    private Long questionId;

    private String answer;
    private List<Long> selectedOptionIds;
}
