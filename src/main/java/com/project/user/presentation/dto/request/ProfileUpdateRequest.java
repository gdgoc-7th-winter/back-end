package com.project.user.presentation.dto.request;

import com.project.user.domain.enums.Interest;
import com.project.user.domain.enums.TechStack;
import com.project.user.domain.enums.Track;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileUpdateRequest {

    @NotBlank(message = "닉네임은 필수입니다.")
    private String nickname;

    private String studentId;

    @NotNull(message = "학과는 필수 선택 사항입니다.")
    private String department;

    private String profilePicture;

    @NotNull(message="트랙을 선택해주세요")
    private Track track;

    // 복수 선택 가능한 Enum들
    private Set<TechStack> techStacks;
    private Set<Interest> interests;
}
