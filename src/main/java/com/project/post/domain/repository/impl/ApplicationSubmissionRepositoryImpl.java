package com.project.post.domain.repository.impl;

import com.project.post.domain.entity.QApplicationSubmission;
import com.project.post.domain.entity.QPost;
import com.project.post.domain.entity.QRecruitingApplication;
import com.project.post.domain.entity.QRecruitingPost;
import com.project.post.domain.repository.ApplicationSubmissionRepositoryCustom;
import com.project.post.domain.repository.dto.AppliedRecruitingPostListQueryResult;
import com.project.user.domain.entity.QDepartment;
import com.project.user.domain.entity.QLevelBadge;
import com.project.user.domain.entity.QUser;
import com.project.user.domain.repository.querydsl.UserRepresentativeTrackExpressions;
import com.project.user.domain.repository.querydsl.UserWithdrawnExpressions;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

@Repository
@RequiredArgsConstructor
public class ApplicationSubmissionRepositoryImpl implements ApplicationSubmissionRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<AppliedRecruitingPostListQueryResult> findAppliedRecruitingPostListByUserId(
            Long userId,
            Pageable pageable
    ) {
        QApplicationSubmission submission = QApplicationSubmission.applicationSubmission;
        QRecruitingApplication recruitingApplication = QRecruitingApplication.recruitingApplication;
        QRecruitingPost recruitingPost = QRecruitingPost.recruitingPost;
        QPost post = QPost.post;
        QUser author = QUser.user;
        QDepartment department = QDepartment.department;
        QLevelBadge levelBadge = QLevelBadge.levelBadge;

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
                .where(
                        submission.user.id.eq(userId),
                        submission.deletedAt.isNull(),
                        post.deletedAt.isNull()
                )
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
                            .where(
                                    submission.user.id.eq(userId),
                                    submission.deletedAt.isNull(),
                                    post.deletedAt.isNull()
                            )
                            .fetchOne();

                    return count == null ? 0L : count;
                }
        );
    }
}
