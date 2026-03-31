package com.project.user.application.dto.response;

import com.project.user.domain.entity.SocialAccount;
import com.project.user.domain.entity.User;
import com.project.user.domain.enums.Authority;

import java.util.List;
import java.util.Set;

public record ProfileResponse(
        String email,
        String nickname,
        String studentId,
        String college,
        String departmentName,
        List<String> tracks,
        Integer userPoint,
        String levelBadgeName,
        String profileImgUrl,
        Authority authority,
        List<String> techStacks,
        String introduction,
        Set<SocialAccount> socialAccounts,
        boolean isWithdrawn
) {
    public static ProfileResponse from(User user) {
        return new ProfileResponse(
                user.getEmail(),
                user.getNickname(),
                user.getStudentId(),
                user.getDepartment() != null ? user.getDepartment().getCollege() : null,
                user.getDepartment() != null ? user.getDepartment().getName() : null,
                user.getUserTracks().stream()
                        .map(ut -> ut.getTrack().getName())
                        .toList(),
                user.getTotalPoint(),
                user.getLevelBadge() != null ? user.getLevelBadge().getLevelName() : null,
                user.getProfileImgUrl(),
                user.getAuthority(),
                user.getUserTechStacks().stream()
                        .map(uts -> uts.getTechStack().getName())
                        .toList(),
                user.getIntroduction(),
                user.getSocialAccounts(),
                user.isDeleted()
        );
    }
}
