package com.project.post.domain.repository.impl;

import com.project.post.domain.entity.QPost;
import com.project.post.domain.entity.QRecruitingPost;
import com.project.post.domain.enums.RecruitingCategory;
import com.project.post.domain.repository.RecruitingPostRepositoryCustom;
import com.project.post.domain.repository.dto.RecruitingPostListQueryResult;
import com.project.user.domain.entity.QDepartment;
import com.project.user.domain.entity.QLevelBadge;
import com.project.user.domain.entity.QUser;
import com.project.user.domain.repository.querydsl.UserRepresentativeTrackExpressions;
import com.project.user.domain.repository.querydsl.UserWithdrawnExpressions;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Repository
@RequiredArgsConstructor
public class RecruitingPostRepositoryImpl implements RecruitingPostRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<RecruitingPostListQueryResult> findRecruitingPostList(
            @Nullable RecruitingCategory category,
            @NonNull Pageable pageable
    ) {
        QRecruitingPost recruitingPost = QRecruitingPost.recruitingPost;
        QPost post = QPost.post;
        QUser user = QUser.user;
        QDepartment department = QDepartment.department;
        QLevelBadge levelBadge = QLevelBadge.levelBadge;

        BooleanBuilder where = new BooleanBuilder();
        where.and(recruitingPost.deletedAt.isNull());
        where.and(post.deletedAt.isNull());

        if (category != null) {
            where.and(recruitingPost.category.eq(category));
        }

        var projection = Projections.constructor(
                RecruitingPostListQueryResult.class,
                recruitingPost.category,
                recruitingPost.applicationType,
                recruitingPost.startedAt,
                recruitingPost.deadlineAt,

                post.id,
                post.title,
                post.content,
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

        List<RecruitingPostListQueryResult> content = queryFactory
                .select(projection)
                .from(recruitingPost)
                .join(recruitingPost.post, post)
                .join(post.author, user)
                .leftJoin(user.department, department)
                .leftJoin(user.levelBadge, levelBadge)
                .where(where)
                .orderBy(toOrderSpecifiers(pageable, post))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        return PageableExecutionUtils.getPage(
                Objects.requireNonNull(content),
                pageable,
                () -> fetchRecruitingPostListCount(where, recruitingPost, post)
        );
    }

    private long fetchRecruitingPostListCount(
            BooleanBuilder where,
            QRecruitingPost recruitingPost,
            QPost post
    ) {
        Long total = queryFactory
                .select(recruitingPost.id.count())
                .from(recruitingPost)
                .join(recruitingPost.post, post)
                .where(where)
                .fetchOne();

        return total == null ? 0L : total;
    }

    private OrderSpecifier<?>[] toOrderSpecifiers(Pageable pageable, QPost post) {
        List<OrderSpecifier<?>> specifiers = new ArrayList<>();

        for (Sort.Order order : pageable.getSort()) {
            String prop = order.getProperty();

            if ("createdAt".equalsIgnoreCase(prop)) {
                specifiers.add(order.isAscending() ? post.createdAt.asc() : post.createdAt.desc());
            } else if ("id".equalsIgnoreCase(prop)) {
                specifiers.add(order.isAscending() ? post.id.asc() : post.id.desc());
            } else if ("viewCount".equalsIgnoreCase(prop)) {
                specifiers.add(order.isAscending() ? post.viewCount.asc() : post.viewCount.desc());
            } else if ("likeCount".equalsIgnoreCase(prop)) {
                specifiers.add(order.isAscending() ? post.likeCount.asc() : post.likeCount.desc());
            } else if ("scrapCount".equalsIgnoreCase(prop)) {
                specifiers.add(order.isAscending() ? post.scrapCount.asc() : post.scrapCount.desc());
            } else if ("commentCount".equalsIgnoreCase(prop)) {
                specifiers.add(order.isAscending() ? post.commentCount.asc() : post.commentCount.desc());
            }
        }

        if (specifiers.isEmpty()) {
            specifiers.add(post.createdAt.desc());
        }

        specifiers.add(post.id.desc());
        return specifiers.toArray(OrderSpecifier[]::new);
    }
}
