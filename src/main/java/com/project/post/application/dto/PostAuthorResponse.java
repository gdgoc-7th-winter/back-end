package com.project.post.application.dto;

public record PostAuthorResponse(
        Long authorId,
        String nickname,
        String profileImageUrl,
        String departmentName,
        String representativeTrackName,
        String tierBadgeImageUrl,
        boolean isWithdrawn
) {

    public static PostAuthorResponse fromParts(
            Long authorId,
            String nickname,
            String profileImageUrl,
            String departmentName,
            String representativeTrackName,
            String tierBadgeImageUrl,
            boolean isWithdrawn) {
        return new PostAuthorResponse(
                authorId,
                nickname,
                profileImageUrl,
                departmentName,
                representativeTrackName,
                tierBadgeImageUrl,
                isWithdrawn
        );
    }
}
