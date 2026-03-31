package com.project.post.domain.repository.impl;

import com.project.post.domain.entity.QPost;
import com.project.post.domain.entity.QPostAttachment;
import com.project.post.domain.entity.QPostTag;
import com.project.post.domain.entity.QTag;
import com.project.post.domain.repository.PostRepositoryCustom;
import com.project.post.domain.repository.dto.PostDetailQueryResult;
import com.project.post.domain.repository.dto.PostListQueryResult;
import com.project.post.domain.repository.dto.PostSearchCondition;
import com.project.post.domain.repository.querydsl.PostListSortOrderSpecifiers;
import com.project.post.domain.repository.querydsl.QuerydslLikeExpressions;
import com.project.user.domain.repository.querydsl.UserRepresentativeTrackExpressions;
import com.project.user.domain.repository.querydsl.UserWithdrawnExpressions;
import com.project.user.domain.entity.QDepartment;
import com.project.user.domain.entity.QLevelBadge;
import com.project.user.domain.entity.QUser;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.ConstructorExpression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
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
        BooleanBuilder where = buildPostListWhere(boardCode, condition, post);
        return fetchPostListPage(pageable, condition, post, where, postListProjection(post));
    }

    @Override
    public Page<PostListQueryResult> findPostListAllActiveBoards(
            @NonNull Pageable pageable,
            @NonNull PostSearchCondition condition) {
        QPost post = QPost.post;
        BooleanBuilder where = buildPostListWhere(null, condition, post);
        return fetchPostListPage(pageable, condition, post, where, postListProjection(post));
    }

    private ConstructorExpression<PostListQueryResult> postListProjection(QPost post) {
        QUser user = QUser.user;
        QDepartment department = QDepartment.department;
        QLevelBadge levelBadge = QLevelBadge.levelBadge;
        return Projections.constructor(
                PostListQueryResult.class,
                post.id,
                post.title,
                post.thumbnailUrl,
                user.id,
                user.nickname,
                user.profileImgUrl,
                department.name,
                UserRepresentativeTrackExpressions.representativeTrackNameSubquery(user),
                levelBadge.levelImage,
                UserWithdrawnExpressions.authorIsWithdrawn(user),
                post.viewCount,
                post.likeCount,
                post.scrapCount,
                post.commentCount,
                post.createdAt
        );
    }

    private Page<PostListQueryResult> fetchPostListPage(
            Pageable pageable,
            PostSearchCondition condition,
            QPost post,
            BooleanBuilder where,
            ConstructorExpression<PostListQueryResult> projection) {
        QUser user = QUser.user;
        QDepartment department = QDepartment.department;
        QLevelBadge levelBadge = QLevelBadge.levelBadge;
        var content = queryFactory
                .select(projection)
                .from(post)
                .join(post.author, user)
                .leftJoin(user.department, department)
                .leftJoin(user.levelBadge, levelBadge)
                .where(where)
                .orderBy(PostListSortOrderSpecifiers.forPost(condition.sort(), post))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return PageableExecutionUtils.getPage(
                Objects.requireNonNull(content),
                pageable,
                () -> fetchPostListCount(post, where)
        );
    }

    private long fetchPostListCount(QPost post, BooleanBuilder where) {
        QUser user = QUser.user;
        Long total = queryFactory
                .select(post.id.count())
                .from(post)
                .join(post.author, user)
                .where(where)
                .fetchOne();
        return total == null ? 0L : total;
    }

    @Override
    public Optional<PostDetailQueryResult> findPostDetail(@NonNull Long postId) {
        QPost post = QPost.post;
        QUser user = QUser.user;
        QDepartment department = QDepartment.department;
        QLevelBadge levelBadge = QLevelBadge.levelBadge;
        Expression<String> representativeTrackName = UserRepresentativeTrackExpressions.representativeTrackNameSubquery(user);
        Expression<Boolean> authorWithdrawn = UserWithdrawnExpressions.authorIsWithdrawn(user);

        Tuple base = queryFactory
                .select(
                        post.id,
                        post.title,
                        post.content,
                        post.thumbnailUrl,
                        user.id,
                        user.nickname,
                        user.profileImgUrl,
                        department.name,
                        representativeTrackName,
                        levelBadge.levelImage,
                        authorWithdrawn,
                        post.viewCount,
                        post.likeCount,
                        post.scrapCount,
                        post.commentCount,
                        post.createdAt,
                        post.updatedAt
                )
                .from(post)
                .join(post.author, user)
                .leftJoin(user.department, department)
                .leftJoin(user.levelBadge, levelBadge)
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

        return Optional.of(buildDetailResult(
                base,
                new PostDetailTupleProjection(
                        post, user, department, levelBadge, representativeTrackName, authorWithdrawn),
                tagNames,
                attachments));
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
            PostDetailTupleProjection p,
            List<String> tagNames,
            List<PostDetailQueryResult.AttachmentDto> attachments) {
        QPost post = p.post();
        QUser user = p.user();
        QDepartment department = p.department();
        QLevelBadge levelBadge = p.levelBadge();
        return new PostDetailQueryResult(
                base.get(post.id),
                base.get(post.title),
                base.get(post.content),
                base.get(post.thumbnailUrl),
                base.get(user.id),
                base.get(user.nickname),
                base.get(user.profileImgUrl),
                base.get(department.name),
                base.get(p.representativeTrackName()),
                base.get(levelBadge.levelImage),
                base.get(p.authorWithdrawn()),
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

    private record PostDetailTupleProjection(
            QPost post,
            QUser user,
            QDepartment department,
            QLevelBadge levelBadge,
            Expression<String> representativeTrackName,
            Expression<Boolean> authorWithdrawn) {
    }

    private BooleanBuilder buildPostListWhere(
            @Nullable String boardCode,
            PostSearchCondition condition,
            QPost post) {
        BooleanBuilder where = new BooleanBuilder();
        where.and(post.board.active.isTrue());
        if (boardCode != null) {
            where.and(post.board.code.eq(boardCode));
        }
        where.and(post.deletedAt.isNull());

        if (condition.hasKeyword()) {
            String escapedKeyword = QuerydslLikeExpressions.escapeLikeWildcard(condition.keyword());
            BooleanExpression keywordMatch = QuerydslLikeExpressions.likeIgnoreCaseContains(post.title, escapedKeyword)
                    .or(QuerydslLikeExpressions.likeIgnoreCaseContains(post.content, escapedKeyword));
            QPostTag keywordPostTag = new QPostTag("keywordPostTag");
            QTag keywordTag = new QTag("keywordTag");
            BooleanExpression tagKeywordMatch = JPAExpressions.selectOne()
                    .from(keywordPostTag)
                    .join(keywordPostTag.tag, keywordTag)
                    .where(
                            keywordPostTag.post.eq(post),
                            QuerydslLikeExpressions.likeIgnoreCaseContains(keywordTag.name, escapedKeyword)
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
}
