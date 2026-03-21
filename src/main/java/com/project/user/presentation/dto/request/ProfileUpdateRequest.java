package com.project.user.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
    @Size(max = 50, message = "닉네임은 50자를 초과할 수 없습니다.")
    private String nickname;

    @NotBlank(message = "학번은 필수 입력 사항입니다.")
    @Size(max = 20, message = "학번은 20자를 초과할 수 없습니다.")
    private String studentId;

    @NotNull(message = "학과는 필수 선택 사항입니다.")
    private Long departmentId;

    private String profilePicture;

    @NotEmpty(message = "트랙을 하나 이상 선택해주세요.")
    private List<@NotBlank(message = "트랙 이름은 공백일 수 없습니다.") String> trackNames;

    private List<String> techStackNames;

    private String introduction;
}
