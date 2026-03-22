package com.project.post.application.dto;

import com.project.user.domain.entity.User;

public record PostAuthorResponse(
        Long authorId,
        String nickname,
        String profileImageUrl,
        String departmentName,
        String representativeTrackName,
        String tierBadgeImageUrl
) {

    public static PostAuthorResponse fromParts(
            Long authorId,
            String nickname,
            String profileImageUrl,
            String departmentName,
            String representativeTrackName,
            String tierBadgeImageUrl) {
        return new PostAuthorResponse(
                authorId,
                nickname,
                profileImageUrl,
                departmentName,
                representativeTrackName,
                tierBadgeImageUrl
        );
    }

    public static PostAuthorResponse fromUser(User user) {
        if (user == null) {
            return null;
        }
        String departmentName = user.getDepartment() != null ? user.getDepartment().getName() : null;
        String tierUrl = user.getLevelBadge() != null ? user.getLevelBadge().getLevelImage() : null;
        return new PostAuthorResponse(
                user.getId(),
                user.getNickname(),
                user.getProfileImgUrl(),
                departmentName,
                user.getRepresentativeTrackName(),
                tierUrl
        );
    }
}
