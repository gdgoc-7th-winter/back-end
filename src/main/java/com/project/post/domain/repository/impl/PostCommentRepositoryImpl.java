package com.project.post.domain.repository.impl;

import com.project.post.domain.repository.PostCommentRepositoryCustom;
import com.project.post.domain.entity.PostComment;
import com.project.post.domain.entity.QPostComment;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Repository
@RequiredArgsConstructor
public class PostCommentRepositoryImpl implements PostCommentRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<PostComment> findRootComments(@NonNull Long postId, @NonNull Pageable pageable) {
        QPostComment comment = QPostComment.postComment;

        List<PostComment> content = queryFactory
                .selectFrom(comment)
                .join(comment.user).fetchJoin()
                .join(comment.post).fetchJoin()
                .where(
                        comment.post.id.eq(postId),
                        comment.deletedAt.isNull(),
                        comment.parentComment.isNull()
                )
                .orderBy(comment.createdAt.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(comment.count())
                .from(comment)
                .where(
                        comment.post.id.eq(postId),
                        comment.deletedAt.isNull(),
                        comment.parentComment.isNull()
                )
                .fetchOne();

        long totalCount = total == null ? 0L : total;
        return new PageImpl<>(Objects.requireNonNull(content), pageable, totalCount);
    }

    @Override
    public List<PostComment> findRepliesByParentIds(@NonNull List<Long> parentIds) {
        if (parentIds == null || parentIds.isEmpty()) {
            return Collections.emptyList();
        }
        QPostComment comment = QPostComment.postComment;
        return queryFactory
                .selectFrom(comment)
                .join(comment.user).fetchJoin()
                .join(comment.post).fetchJoin()
                .join(comment.parentComment).fetchJoin()
                .where(
                        comment.parentComment.id.in(parentIds),
                        comment.deletedAt.isNull()
                )
                .orderBy(comment.createdAt.asc())
                .fetch();
    }
}
