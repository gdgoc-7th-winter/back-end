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
import java.util.Objects;

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
        QApplicationSubmission submission = QApplicationSubmission.applicationSubmission;
        QRecruitingApplication recruitingApplication = QRecruitingApplication.recruitingApplication;
        QRecruitingPost recruitingPost = QRecruitingPost.recruitingPost;
        QPost post = QPost.post;
        QUser author = QUser.user;
        QDepartment department = QDepartment.department;
        QLevelBadge levelBadge = QLevelBadge.levelBadge;

        Expression<String> contentPreview = Expressions.stringTemplate(
                "SUBSTRING({0}, 1, 101)",
                post.content
        );

        BooleanBuilder where = new BooleanBuilder();
        where.and(submission.user.id.eq(userId));
        where.and(submission.deletedAt.isNull());
        where.and(recruitingPost.deletedAt.isNull());
        where.and(post.deletedAt.isNull());

        if (status != null) {
            switch (status) {
                case UPCOMING -> where.and(
                        recruitingPost.startedAt.isNotNull()
                                .and(recruitingPost.startedAt.gt(now))
                );
                case CLOSED -> where.and(
                        recruitingPost.startedAt.isNull().or(recruitingPost.startedAt.loe(now))
                ).and(
                        recruitingPost.deadlineAt.isNotNull()
                                .and(recruitingPost.deadlineAt.lt(now))
                );
                case OPEN -> where.and(
                        recruitingPost.startedAt.isNull().or(recruitingPost.startedAt.loe(now))
                ).and(
                        recruitingPost.deadlineAt.isNull().or(recruitingPost.deadlineAt.goe(now))
                );
                default -> throw new IllegalArgumentException("Invalid RecruitingStatus: " + status);
            }
        }

        List<AppliedRecruitingPostListQueryResult> content = queryFactory
                .select(Projections.constructor(
                        AppliedRecruitingPostListQueryResult.class,
                        submission.id,

                        recruitingPost.category,
                        recruitingPost.startedAt,
                        recruitingPost.deadlineAt,
                        submission.submittedAt,

                        post.id,
                        post.title,
                        contentPreview,
                        post.thumbnailUrl,

                        author.id,
                        author.nickname,
                        author.profileImgUrl,
                        department.name,
                        UserRepresentativeTrackExpressions.representativeTrackNameSubquery(author),
                        levelBadge.levelImage,
                        UserWithdrawnExpressions.authorIsWithdrawn(author),

                        post.viewCount,
                        post.likeCount,
                        post.scrapCount,
                        post.commentCount,

                        post.createdAt
                ))
                .from(submission)
                .join(submission.recruitingApplication, recruitingApplication)
                .join(recruitingApplication.recruitingPost, recruitingPost)
                .join(recruitingPost.post, post)
                .join(post.author, author)
                .leftJoin(author.department, department)
                .leftJoin(author.levelBadge, levelBadge)
                .where(where)
                .orderBy(submission.submittedAt.desc(), submission.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return PageableExecutionUtils.getPage(
                Objects.requireNonNull(content),
                pageable,
                () -> {
                    Long count = queryFactory
                            .select(submission.count())
                            .from(submission)
                            .join(submission.recruitingApplication, recruitingApplication)
                            .join(recruitingApplication.recruitingPost, recruitingPost)
                            .join(recruitingPost.post, post)
                            .where(where)
                            .fetchOne();

                    return count == null ? 0L : count;
                }
        );
    }
}