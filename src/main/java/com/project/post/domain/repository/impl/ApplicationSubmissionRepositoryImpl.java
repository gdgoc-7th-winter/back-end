package com.project.post.domain.repository.impl;

import com.project.post.domain.entity.QApplicationSubmission;
import com.project.post.domain.entity.QPost;
import com.project.post.domain.entity.QRecruitingApplication;
import com.project.post.domain.entity.QRecruitingPost;
import com.project.post.domain.enums.RecruitingStatus;
import com.project.post.domain.repository.ApplicationSubmissionRepositoryCustom;
import com.project.post.domain.repository.dto.AppliedRecruitingPostListQueryResult;
import com.project.user.domain.entity.QDepartment;
import com.project.user.domain.entity.QLevelBadge;
import com.project.user.domain.entity.QUser;
import com.project.user.domain.repository.querydsl.UserRepresentativeTrackExpressions;
import com.project.user.domain.repository.querydsl.UserWithdrawnExpressions;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ApplicationSubmissionRepositoryImpl implements ApplicationSubmissionRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<AppliedRecruitingPostListQueryResult> findAppliedRecruitingPostListByUserId(
            Long userId,
            @Nullable RecruitingStatus status,
            Instant now,
            Pageable pageable
    ) {
        QEntities q = new QEntities();

        BooleanBuilder where = buildWhereCondition(userId, status, now, q);
        Expression<String> contentPreview = createContentPreview(q.post);
        List<AppliedRecruitingPostListQueryResult> content =
                fetchContent(pageable, where, contentPreview, q);

        return PageableExecutionUtils.getPage(
                content,
                pageable,
                () -> fetchCount(where, q)
        );
    }

    private BooleanBuilder buildWhereCondition(
            Long userId,
            @Nullable RecruitingStatus status,
            Instant now,
            QEntities q
    ) {
        BooleanBuilder where = new BooleanBuilder();
        where.and(q.submission.user.id.eq(userId));
        where.and(q.submission.deletedAt.isNull());
        where.and(q.recruitingPost.deletedAt.isNull());
        where.and(q.post.deletedAt.isNull());

        applyStatusCondition(where, status, now, q.recruitingPost);
        return where;
    }

    private void applyStatusCondition(
            BooleanBuilder where,
            @Nullable RecruitingStatus status,
            Instant now,
            QRecruitingPost recruitingPost
    ) {
        if (status == null) {
            return;
        }

        switch (status) {
            case UPCOMING -> where.and(
                    recruitingPost.startedAt.isNotNull()
                            .and(recruitingPost.startedAt.gt(now))
            );
            case CLOSED -> where.and(
                    recruitingPost.startedAt.isNull()
                            .or(recruitingPost.startedAt.loe(now))
            ).and(
                    recruitingPost.deadlineAt.isNotNull()
                            .and(recruitingPost.deadlineAt.lt(now))
            );
            case OPEN -> where.and(
                    recruitingPost.startedAt.isNull()
                            .or(recruitingPost.startedAt.loe(now))
            ).and(
                    recruitingPost.deadlineAt.isNull()
                            .or(recruitingPost.deadlineAt.goe(now))
            );
            default -> throw new IllegalArgumentException("Invalid RecruitingStatus: " + status);
        }
    }

    private Expression<String> createContentPreview(QPost post) {
        return Expressions.stringTemplate(
                "SUBSTRING({0}, 1, 101)",
                post.content
        );
    }

    private List<AppliedRecruitingPostListQueryResult> fetchContent(
            Pageable pageable,
            BooleanBuilder where,
            Expression<String> contentPreview,
            QEntities q
    ) {
        return queryFactory
                .select(Projections.constructor(
                        AppliedRecruitingPostListQueryResult.class,
                        q.submission.id,

                        q.recruitingPost.category,
                        q.recruitingPost.startedAt,
                        q.recruitingPost.deadlineAt,
                        q.submission.submittedAt,

                        q.post.id,
                        q.post.title,
                        contentPreview,
                        q.post.thumbnailUrl,

                        q.author.id,
                        q.author.nickname,
                        q.author.profileImgUrl,
                        q.department.name,
                        UserRepresentativeTrackExpressions.representativeTrackNameSubquery(q.author),
                        q.levelBadge.levelImage,
                        UserWithdrawnExpressions.authorIsWithdrawn(q.author),

                        q.post.viewCount,
                        q.post.likeCount,
                        q.post.scrapCount,
                        q.post.commentCount,

                        q.post.createdAt
                ))
                .from(q.submission)
                .join(q.submission.recruitingApplication, q.recruitingApplication)
                .join(q.recruitingApplication.recruitingPost, q.recruitingPost)
                .join(q.recruitingPost.post, q.post)
                .join(q.post.author, q.author)
                .leftJoin(q.author.department, q.department)
                .leftJoin(q.author.levelBadge, q.levelBadge)
                .where(where)
                .orderBy(q.submission.submittedAt.desc(), q.submission.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    private long fetchCount(BooleanBuilder where, QEntities q) {
        Long count = queryFactory
                .select(q.submission.count())
                .from(q.submission)
                .join(q.submission.recruitingApplication, q.recruitingApplication)
                .join(q.recruitingApplication.recruitingPost, q.recruitingPost)
                .join(q.recruitingPost.post, q.post)
                .where(where)
                .fetchOne();

        return count == null ? 0L : count;
    }

    private static class QEntities {
        private final QApplicationSubmission submission =
                QApplicationSubmission.applicationSubmission;
        private final QRecruitingApplication recruitingApplication =
                QRecruitingApplication.recruitingApplication;
        private final QRecruitingPost recruitingPost =
                QRecruitingPost.recruitingPost;
        private final QPost post = QPost.post;
        private final QUser author = QUser.user;
        private final QDepartment department = QDepartment.department;
        private final QLevelBadge levelBadge = QLevelBadge.levelBadge;
    }
}
