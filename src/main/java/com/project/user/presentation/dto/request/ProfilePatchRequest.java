package com.project.user.presentation.dto.request;

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
public class ProfilePatchRequest {
    @Size(max = 50, message = "닉네임은 50자를 초과할 수 없습니다.")
    private String nickname;

    @Size(max = 20, message = "학번은 20자를 초과할 수 없습니다.")
    private String studentId;
    private Long departmentId;
    private String profilePicture;
    private List<String> trackNames;
    private List<String> techStackNames;
    private String introduction;
}
