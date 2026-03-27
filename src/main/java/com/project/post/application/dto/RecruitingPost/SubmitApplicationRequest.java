package com.project.post.application.dto.RecruitingPost;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class SubmitApplicationRequest {
    private List<AnswerRequest> answers;

    @NotBlank(message = "이름은 필수입니다.")
    private String applicantName;
}