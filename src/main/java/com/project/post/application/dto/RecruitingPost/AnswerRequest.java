package com.project.post.application.dto.RecruitingPost;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class AnswerRequest {

    private Long questionId;
    private String answer;
    private List<Long> selectedOptionIds;
}
