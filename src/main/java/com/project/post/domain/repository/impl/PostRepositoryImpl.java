package com.project.post.domain.repository.impl;

import com.project.post.domain.entity.QPost;
import com.project.post.domain.entity.QPostAttachment;
import com.project.post.domain.entity.QPostTag;
import com.project.post.domain.entity.QTag;
import com.project.post.domain.repository.PostRepositoryCustom;
import com.project.post.domain.repository.dto.PostDetailQueryResult;
import com.project.post.domain.repository.dto.PostListQueryResult;
import com.project.post.domain.enums.PostListSort;
import com.project.post.domain.repository.dto.PostSearchCondition;
import com.project.user.domain.entity.QUser;
import com.querydsl.core.Tuple;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
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
    public Page<PostListQueryResult> findPostList(
            @NonNull String boardCode,
            @NonNull Pageable pageable,
            @NonNull PostSearchCondition condition) {
        QPost post = QPost.post;
        QUser user = QUser.user;

        BooleanBuilder where = buildPostListWhere(boardCode, condition, post);

        var projection = Projections.constructor(
                PostListQueryResult.class,
                post.id,
                post.title,
                post.thumbnailUrl,
                user.nickname,
                post.viewCount,
                post.likeCount,
                post.scrapCount,
                post.commentCount,
                post.createdAt
        );
        var content = queryFactory
                .select(projection)
                .from(post)
                .join(post.author, user)
                .where(where)
                .orderBy(toOrderSpecifiers(condition.sort(), post))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long totalCount = fetchPostListCount(post, where);
        return new PageImpl<>(Objects.requireNonNull(content), pageable, totalCount);
    }

    private long fetchPostListCount(QPost post, BooleanBuilder where) {
        Long total = queryFactory
                .select(post.id.count())
                .from(post)
                .where(where)
                .fetchOne();
        return total == null ? 0L : total;
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

    private BooleanBuilder buildPostListWhere(
            String boardCode,
            PostSearchCondition condition,
            QPost post) {
        BooleanBuilder where = new BooleanBuilder();
        where.and(post.board.code.eq(boardCode));
        where.and(post.deletedAt.isNull());

        if (condition.hasKeyword()) {
            String escapedKeyword = escapeLikeWildcard(condition.keyword());
            BooleanExpression keywordMatch = likeIgnoreCaseWithEscape(post.title, escapedKeyword)
                    .or(likeIgnoreCaseWithEscape(post.content, escapedKeyword));
            QPostTag keywordPostTag = new QPostTag("keywordPostTag");
            QTag keywordTag = new QTag("keywordTag");
            BooleanExpression tagKeywordMatch = JPAExpressions.selectOne()
                    .from(keywordPostTag)
                    .join(keywordPostTag.tag, keywordTag)
                    .where(
                            keywordPostTag.post.eq(post),
                            likeIgnoreCaseWithEscape(keywordTag.name, escapedKeyword)
                    )
                    .exists();
            keywordMatch = keywordMatch.or(tagKeywordMatch);
            where.and(keywordMatch);
        }

        if (condition.hasTags()) {
            QPostTag tagFilter = new QPostTag("tagFilter");
            QTag tagFilterTag = new QTag("tagFilterTag");
            where.and(JPAExpressions.selectOne()
                    .from(tagFilter)
                    .join(tagFilter.tag, tagFilterTag)
                    .where(
                            tagFilter.post.eq(post),
                            tagFilterTag.name.in(condition.tagNames())
                    )
                    .exists());
        }

        return where;
    }

    private OrderSpecifier<?>[] toOrderSpecifiers(PostListSort sort, QPost post) {
        List<OrderSpecifier<?>> specifiers = new ArrayList<>();
        if (sort == PostListSort.VIEWS) {
            specifiers.add(post.viewCount.desc());
            specifiers.add(post.createdAt.desc());
        } else if (sort == PostListSort.LIKES) {
            specifiers.add(post.likeCount.desc());
            specifiers.add(post.createdAt.desc());
        } else {
            specifiers.add(post.createdAt.desc());
        }
        specifiers.add(post.id.desc());
        return specifiers.toArray(new OrderSpecifier<?>[0]);
    }

    private static String escapeLikeWildcard(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        return value
                .replace("!", "!!")
                .replace("%", "!%")
                .replace("_", "!_");
    }

    private static BooleanExpression likeIgnoreCaseWithEscape(
            com.querydsl.core.types.dsl.StringExpression expr,
            String escapedKeyword) {
        return Expressions.booleanTemplate(
                "LOWER({0}) LIKE LOWER(CONCAT('%', {1}, '%')) ESCAPE '!'",
                expr,
                escapedKeyword
        );
    }
}
