package com.project.post.application.dto.RecruitingPost;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class SubmitApplicationRequest {

    private Long recruitingPostId;
    private List<AnswerRequest> answers;
}