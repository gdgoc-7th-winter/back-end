package com.project.user.presentation.dto.request;

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
    private String nickname;
    private String studentId;
    private Long departmentId;
    private String profilePicture;
    private List<String> trackNames;
    private List<String> techStackNames;
    private String introduction;
}
