package com.project.post.application.dto;

import com.project.user.domain.entity.User;
import com.project.user.domain.entity.UserTrack;

import java.util.Comparator;
import java.util.Objects;

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
        String representativeTrack = user.getUserTracks().stream()
                .filter(Objects::nonNull)
                .filter(ut -> ut.getTrack() != null)
                .min(Comparator.comparing(UserTrack::getId, Comparator.nullsLast(Long::compareTo)))
                .map(ut -> ut.getTrack().getName())
                .orElse(null);
        String tierUrl = user.getLevelBadge() != null ? user.getLevelBadge().getLevelImage() : null;
        return new PostAuthorResponse(
                user.getId(),
                user.getNickname(),
                user.getProfileImgUrl(),
                departmentName,
                representativeTrack,
                tierUrl
        );
    }
}
