package com.project.user.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileUpdateRequest {

    @NotBlank(message = "닉네임은 필수 입력 사항입니다.")
    private String nickname;

    @NotBlank(message = "학번은 필수 입력 사항입니다.")
    private String studentId;

    @NotNull(message = "학과는 필수 선택 사항입니다.")
    private Long departmentId;

    private String profilePicture;

    @NotNull(message="트랙을 선택해주세요")
    private List<String> trackNames;

    private List<String> techStackNames;

    private String introduction;
}
