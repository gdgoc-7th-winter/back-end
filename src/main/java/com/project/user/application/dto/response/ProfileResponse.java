package com.project.user.application.dto.response;

import com.project.user.domain.entity.User;
import com.project.user.domain.enums.Authority;

import java.util.Set;
import java.util.stream.Collectors;

public record ProfileResponse(
        String email,
        String nickname,
        String studentId,
        String department,
        String track,
        Integer userPoint,
        String levelBadgeName,
        String profileImgUrl,
        Authority authority,
        Set<String> techStacks,
        Set<String> interests
) {
    public static ProfileResponse from(User user) {
        return new ProfileResponse(
                user.getEmail(),
                user.getNickname(),
                user.getStudentId(),
                user.getDepartment(),
                user.getTrack() != null ? user.getTrack().name() : null,
                user.getTotalPoint(),
                user.getLevelBadge() != null ? user.getLevelBadge().getLevelName() : null,
                user.getProfileImgUrl(),
                user.getAuthority(),
                user.getTechStacks().stream().map(Enum::name).collect(Collectors.toSet()),
                user.getInterests().stream().map(Enum::name).collect(Collectors.toSet())
        );
    }
}
