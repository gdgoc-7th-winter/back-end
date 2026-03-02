package com.project.post.domain.repository.impl;

import com.project.post.domain.repository.PostRepositoryCustom;
import com.project.post.domain.repository.dto.PostDetailQueryResult;
import com.project.post.domain.repository.dto.PostListQueryResult;
import com.project.post.domain.entity.QPost;
import com.project.post.domain.entity.QPostAttachment;
import com.project.post.domain.entity.QPostTag;
import com.project.post.domain.entity.QTag;
import com.project.user.domain.entity.QUser;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<PostListQueryResult> findPostList(@NonNull String boardCode, @NonNull Pageable pageable) {
        QPost post = QPost.post;
        QUser user = QUser.user;

        var content = queryFactory
                .select(Projections.constructor(
                        PostListQueryResult.class,
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

        Tuple base = queryFactory
                .select(
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
                        post.updatedAt
                )
                .from(post)
                .join(post.author, user)
                .where(
                        post.id.eq(postId),
                        post.deletedAt.isNull()
                )
                .fetchOne();

        if (base == null) {
            return Optional.empty();
        }

        List<String> tagNames = fetchTagNames(postId);
        List<PostDetailQueryResult.AttachmentDto> attachments = fetchAttachments(postId);

        return Optional.of(buildDetailResult(base, post, user, tagNames, attachments));
    }

    private List<String> fetchTagNames(Long postId) {
        QPostTag postTag = QPostTag.postTag;
        QTag tag = QTag.tag;
        List<String> tagNames = new ArrayList<>(queryFactory
                .select(tag.name)
                .from(postTag)
                .join(postTag.tag, tag)
                .where(postTag.post.id.eq(postId))
                .fetch());
        tagNames.removeIf(Objects::isNull);
        return tagNames;
    }

    private List<PostDetailQueryResult.AttachmentDto> fetchAttachments(Long postId) {
        QPostAttachment attachment = QPostAttachment.postAttachment;
        return new ArrayList<>(queryFactory
                .select(Projections.constructor(
                        PostDetailQueryResult.AttachmentDto.class,
                        attachment.fileUrl,
                        attachment.fileName,
                        attachment.contentType,
                        attachment.fileSize,
                        attachment.sortOrder
                ))
                .from(attachment)
                .where(attachment.post.id.eq(postId))
                .orderBy(attachment.sortOrder.asc())
                .fetch());
    }

    private PostDetailQueryResult buildDetailResult(
            Tuple base,
            QPost post,
            QUser user,
            List<String> tagNames,
            List<PostDetailQueryResult.AttachmentDto> attachments) {
        return new PostDetailQueryResult(
                base.get(post.id),
                base.get(post.title),
                base.get(post.content),
                base.get(post.thumbnailUrl),
                base.get(user.nickname),
                base.get(user.id),
                base.get(post.viewCount),
                base.get(post.likeCount),
                base.get(post.scrapCount),
                base.get(post.commentCount),
                base.get(post.createdAt),
                base.get(post.updatedAt),
                tagNames,
                attachments
        );
    }
}
