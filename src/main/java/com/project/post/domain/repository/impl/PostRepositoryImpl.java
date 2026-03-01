package com.project.post.domain.repository.impl;

import com.project.post.domain.repository.PostRepositoryCustom;
import com.project.post.domain.repository.dto.PostDetailQueryResult;
import com.project.post.application.dto.PostListResponse;
import com.project.post.domain.entity.QPost;
import com.project.post.domain.entity.QPostAttachment;
import com.project.post.domain.entity.QPostTag;
import com.project.post.domain.entity.QTag;
import com.project.user.domain.entity.QUser;
import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<PostListResponse> findPostList(@NonNull String boardCode, @NonNull Pageable pageable) {
        QPost post = QPost.post;
        QUser user = QUser.user;

        var content = queryFactory
                .select(Projections.constructor(
                        PostListResponse.class,
                        post.id,
                        post.title,
                        post.thumbnailUrl,
                        user.nickname,
                        post.viewCount,
                        post.likeCount,
                        post.commentCount,
                        post.createdAt
                ))
                .from(post)
                .join(post.author, user)
                .where(
                        post.board.code.eq(boardCode),
                        post.deletedAt.isNull()
                )
                .orderBy(post.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(post.count())
                .from(post)
                .where(
                        post.board.code.eq(boardCode),
                        post.deletedAt.isNull()
                )
                .fetchOne();

        long totalCount = total == null ? 0L : total;
        return new PageImpl<>(Objects.requireNonNull(content), pageable, totalCount);
    }

    @Override
    public Optional<PostDetailQueryResult> findPostDetail(@NonNull Long postId) {
        QPost post = QPost.post;
        QUser user = QUser.user;
        QPostTag postTag = QPostTag.postTag;
        QTag tag = QTag.tag;
        QPostAttachment attachment = QPostAttachment.postAttachment;

        Map<Long, PostDetailQueryResult> result = queryFactory
                .from(post)
                .join(post.author, user)
                .leftJoin(postTag).on(postTag.post.id.eq(post.id))
                .leftJoin(tag).on(postTag.tag.id.eq(tag.id))
                .leftJoin(attachment).on(attachment.post.id.eq(post.id))
                .where(
                        post.id.eq(postId),
                        post.deletedAt.isNull()
                )
                .transform(GroupBy.groupBy(post.id).as(
                        Projections.constructor(
                                PostDetailQueryResult.class,
                                post.id,
                                post.title,
                                post.content,
                                post.thumbnailUrl,
                                user.nickname,
                                user.id,
                                post.viewCount,
                                post.likeCount,
                                post.scrapCount,
                                post.commentCount,
                                post.createdAt,
                                post.updatedAt,
                                GroupBy.set(tag.name),
                                GroupBy.set(Projections.constructor(
                                        PostDetailQueryResult.AttachmentDto.class,
                                        attachment.fileUrl,
                                        attachment.fileName,
                                        attachment.contentType,
                                        attachment.fileSize,
                                        attachment.sortOrder
                                ))
                        )
                ));

        return Optional.ofNullable(result.get(postId));
    }
}
