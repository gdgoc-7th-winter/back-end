package com.project.post.application.dto.RecruitingPost;

import com.project.post.domain.enums.Campus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class SubmitApplicationRequest {

    @NotEmpty(message = "답변은 필수입니다.")
    private List<AnswerRequest> answers;

    @NotBlank(message = "이름은 필수입니다.")
    private String applicantName;

    @NotNull(message = "캠퍼스는 필수입니다.")
    private Campus campus;

    @NotBlank(message = "학과는 필수입니다.")
    private String department;
}