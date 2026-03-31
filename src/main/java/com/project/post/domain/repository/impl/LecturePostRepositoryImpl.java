package com.project.post.domain.repository.impl;

import com.project.post.domain.entity.QLecturePost;
import com.project.post.domain.entity.QPost;
import com.project.post.domain.entity.QPostAttachment;
import com.project.post.domain.entity.QPostTag;
import com.project.post.domain.entity.QTag;
import com.project.post.domain.repository.LecturePostRepositoryCustom;
import com.project.post.domain.repository.dto.LecturePostDetailQueryResult;
import com.project.post.domain.repository.dto.LecturePostListQueryResult;
import com.project.post.domain.repository.dto.LecturePostSearchCondition;
import com.project.post.domain.repository.dto.PostDetailQueryResult;
import com.project.post.domain.repository.querydsl.PostListSortOrderSpecifiers;
import com.project.post.domain.repository.querydsl.QuerydslLikeExpressions;
import com.project.user.domain.repository.querydsl.UserRepresentativeTrackExpressions;
import com.project.user.domain.repository.querydsl.UserWithdrawnExpressions;
import com.project.user.domain.entity.QDepartment;
import com.project.user.domain.entity.QLevelBadge;
import com.project.user.domain.entity.QUser;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class LecturePostRepositoryImpl implements LecturePostRepositoryCustom {

    private static final String BOARD_CODE = "LECTURE";

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<LecturePostListQueryResult> findLecturePostList(
            @NonNull Pageable pageable,
            @NonNull LecturePostSearchCondition condition) {

        QPost post = QPost.post;
        QUser user = QUser.user;
        QLecturePost lecturePost = QLecturePost.lecturePost;
        QDepartment department = QDepartment.department;
        QLevelBadge levelBadge = QLevelBadge.levelBadge;

        BooleanBuilder where = buildListWhere(condition, post, lecturePost);

        var projection = Projections.constructor(
                LecturePostListQueryResult.class,
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
                lecturePost.department,
                lecturePost.campus,
                post.viewCount,
                post.likeCount,
                post.scrapCount,
                post.commentCount,
                post.createdAt
        );

        List<LecturePostListQueryResult> content = queryFactory
                .select(projection)
                .from(lecturePost)
                .join(lecturePost.post, post)
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
                () -> fetchListCount(where, post, lecturePost)
        );
    }

    private long fetchListCount(BooleanBuilder where, QPost post, QLecturePost lecturePost) {
        Long total = queryFactory
                .select(post.id.count())
                .from(lecturePost)
                .join(lecturePost.post, post)
                .where(where)
                .fetchOne();
        return total == null ? 0L : total;
    }

    @Override
    public Optional<LecturePostDetailQueryResult> findLecturePostDetail(@NonNull Long postId) {
        QPost post = QPost.post;
        QUser user = QUser.user;
        QLecturePost lecturePost = QLecturePost.lecturePost;
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
                        lecturePost.department,
                        lecturePost.campus,
                        post.viewCount,
                        post.likeCount,
                        post.scrapCount,
                        post.commentCount,
                        post.createdAt,
                        post.updatedAt
                )
                .from(lecturePost)
                .join(lecturePost.post, post)
                .join(post.author, user)
                .leftJoin(user.department, department)
                .leftJoin(user.levelBadge, levelBadge)
                .where(
                        post.id.eq(postId),
                        post.deletedAt.isNull(),
                        lecturePost.deletedAt.isNull()
                )
                .fetchOne();

        if (base == null) {
            return Optional.empty();
        }

        List<String> tagNames = fetchTagNames(postId);
        List<PostDetailQueryResult.AttachmentDto> attachments = fetchAttachments(postId);

        return Optional.of(new LecturePostDetailQueryResult(
                base.get(post.id),
                base.get(post.title),
                base.get(post.content),
                base.get(post.thumbnailUrl),
                base.get(user.id),
                base.get(user.nickname),
                base.get(user.profileImgUrl),
                base.get(department.name),
                base.get(representativeTrackName),
                base.get(levelBadge.levelImage),
                base.get(authorWithdrawn),
                base.get(lecturePost.department),
                base.get(lecturePost.campus),
                base.get(post.viewCount),
                base.get(post.likeCount),
                base.get(post.scrapCount),
                base.get(post.commentCount),
                base.get(post.createdAt),
                base.get(post.updatedAt),
                tagNames,
                attachments
        ));
    }

    private BooleanBuilder buildListWhere(
            LecturePostSearchCondition condition,
            QPost post,
            QLecturePost lecturePost) {

        BooleanBuilder where = new BooleanBuilder();
        where.and(post.board.code.eq(BOARD_CODE));
        where.and(post.deletedAt.isNull());
        where.and(lecturePost.deletedAt.isNull());

        if (condition.hasCampus()) {
            where.and(lecturePost.campus.eq(condition.campus()));
        }

        if (condition.hasDepartments()) {
            where.and(lecturePost.department.in(condition.departments()));
        }

        if (condition.hasKeyword()) {
            String escapedKeyword = QuerydslLikeExpressions.escapeLikeWildcard(condition.keyword());
            BooleanExpression keywordMatch = QuerydslLikeExpressions.likeIgnoreCaseContains(post.title, escapedKeyword)
                    .or(QuerydslLikeExpressions.likeIgnoreCaseContains(post.content, escapedKeyword))
                    .or(QuerydslLikeExpressions.likeIgnoreCaseContains(lecturePost.department, escapedKeyword));

            BooleanExpression campusMatch = buildCampusKeywordMatch(condition.keyword());
            if (campusMatch != null) {
                keywordMatch = keywordMatch.or(campusMatch);
            }

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

    private BooleanExpression buildCampusKeywordMatch(String keyword) {
        QLecturePost lecturePost = QLecturePost.lecturePost;
        String lower = keyword.trim().toLowerCase();
        if (lower.contains("서울") || lower.contains("seoul")) {
            return lecturePost.campus.eq(com.project.post.domain.enums.Campus.SEOUL);
        }
        if (lower.contains("글로벌") || lower.contains("global")) {
            return lecturePost.campus.eq(com.project.post.domain.enums.Campus.GLOBAL);
        }
        return null;
    }

    private List<String> fetchTagNames(Long postId) {
        QPostTag postTag = QPostTag.postTag;
        QTag tag = QTag.tag;
        List<String> names = new ArrayList<>(queryFactory
                .select(tag.name)
                .from(postTag)
                .join(postTag.tag, tag)
                .where(postTag.post.id.eq(postId))
                .fetch());
        names.removeIf(Objects::isNull);
        return names;
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
}
