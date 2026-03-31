package com.project.post.application.mapper;

import com.project.post.application.dto.PostAuthorResponse;
import com.project.post.domain.repository.dto.LecturePostDetailQueryResult;
import com.project.post.domain.repository.dto.LecturePostListQueryResult;
import com.project.post.domain.repository.dto.PostDetailQueryResult;
import com.project.post.domain.repository.dto.PostListQueryResult;
import com.project.post.domain.repository.dto.PromotionPostListQueryResult;

public final class PostAuthorMapper {

    private PostAuthorMapper() {
    }

    public static PostAuthorResponse from(PostListQueryResult row) {
        return PostAuthorResponse.fromParts(
                row.authorId(),
                row.authorNickname(),
                row.authorProfileImgUrl(),
                row.authorDepartmentName(),
                row.authorRepresentativeTrackName(),
                row.authorTierBadgeImageUrl(),
                row.authorWithdrawn());
    }

    public static PostAuthorResponse from(PostDetailQueryResult row) {
        return PostAuthorResponse.fromParts(
                row.authorId(),
                row.authorNickname(),
                row.authorProfileImgUrl(),
                row.authorDepartmentName(),
                row.authorRepresentativeTrackName(),
                row.authorTierBadgeImageUrl(),
                row.authorWithdrawn());
    }

    public static PostAuthorResponse from(LecturePostListQueryResult row) {
        return PostAuthorResponse.fromParts(
                row.authorId(),
                row.authorNickname(),
                row.authorProfileImgUrl(),
                row.authorDepartmentName(),
                row.authorRepresentativeTrackName(),
                row.authorTierBadgeImageUrl(),
                row.authorWithdrawn());
    }

    public static PostAuthorResponse from(LecturePostDetailQueryResult row) {
        return PostAuthorResponse.fromParts(
                row.authorId(),
                row.authorNickname(),
                row.authorProfileImgUrl(),
                row.authorDepartmentName(),
                row.authorRepresentativeTrackName(),
                row.authorTierBadgeImageUrl(),
                row.authorWithdrawn());
    }

    public static PostAuthorResponse from(PromotionPostListQueryResult row) {
        return PostAuthorResponse.fromParts(
                row.authorId(),
                row.authorNickname(),
                row.authorProfileImgUrl(),
                row.authorDepartmentName(),
                row.authorRepresentativeTrackName(),
                row.authorTierBadgeImageUrl(),
                row.authorWithdrawn());
    }
}
