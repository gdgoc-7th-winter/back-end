package com.project.post.domain.repository.impl;

import com.project.post.domain.entity.PostComment;
import com.project.post.domain.entity.QPostComment;
import com.project.post.domain.repository.PostCommentRepositoryCustom;
import com.project.post.domain.repository.dto.ReplyPreviewRow;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class PostCommentRepositoryImpl implements PostCommentRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final EntityManager entityManager;

    private static final String PREVIEW_SQL = """
            SELECT r.comment_id, r.parent_comment_id
            FROM (
              SELECT
                comment_id,
                parent_comment_id,
                created_at,
                ROW_NUMBER() OVER (
                  PARTITION BY parent_comment_id
                  ORDER BY created_at ASC, comment_id ASC
                ) AS rn
              FROM post_comments
              WHERE post_id = :postId
                AND parent_comment_id IN (:parentIds)
            ) AS r
            WHERE r.rn <= :limitPlusOne
            ORDER BY r.parent_comment_id ASC, r.created_at ASC, r.comment_id ASC
            """;

    @Override
    public List<PostComment> findRootCommentsWithCursor(
            @NonNull Long postId,
            @Nullable Instant cursorCreatedAt,
            @Nullable Long cursorId,
            int limitPlusOne) {
        QPostComment c = QPostComment.postComment;
        BooleanBuilder where = new BooleanBuilder();
        where.and(c.post.id.eq(postId));
        where.and(c.parentComment.isNull());
        if (cursorCreatedAt != null && cursorId != null) {
            where.and(
                    c.createdAt.gt(cursorCreatedAt)
                            .or(c.createdAt.eq(cursorCreatedAt).and(c.id.gt(cursorId))));
        }
        return queryFactory
                .selectFrom(c)
                .join(c.user).fetchJoin()
                .join(c.post).fetchJoin()
                .where(where)
                .orderBy(c.createdAt.asc(), c.id.asc())
                .limit(limitPlusOne)
                .fetch();
    }

    @Override
    public List<ReplyPreviewRow> findReplyPreviewRows(
            @NonNull Long postId,
            @NonNull List<Long> parentCommentIds,
            int limitPlusOne) {
        if (parentCommentIds.isEmpty() || limitPlusOne <= 0) {
            return List.of();
        }
        Query query = entityManager.createNativeQuery(PREVIEW_SQL);
        query.setParameter("postId", postId);
        query.setParameter("parentIds", parentCommentIds);
        query.setParameter("limitPlusOne", limitPlusOne);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        if (rows.isEmpty()) {
            return List.of();
        }
        List<ReplyPreviewRow> out = new ArrayList<>(rows.size());
        for (Object[] row : rows) {
            long commentId = toLong(row[0]);
            long parentId = toLong(row[1]);
            out.add(new ReplyPreviewRow(commentId, parentId));
        }
        return Collections.unmodifiableList(out);
    }

    @Override
    public List<PostComment> findCommentsByParentWithCursor(
            @NonNull Long postId,
            @NonNull Long parentCommentId,
            @Nullable Instant cursorCreatedAt,
            @Nullable Long cursorId,
            int limitPlusOne) {
        QPostComment c = QPostComment.postComment;
        BooleanBuilder where = new BooleanBuilder();
        where.and(c.post.id.eq(postId));
        where.and(c.parentComment.id.eq(parentCommentId));
        if (cursorCreatedAt != null && cursorId != null) {
            where.and(
                    c.createdAt.gt(cursorCreatedAt)
                            .or(c.createdAt.eq(cursorCreatedAt).and(c.id.gt(cursorId))));
        }

        return queryFactory
                .selectFrom(c)
                .join(c.user).fetchJoin()
                .join(c.post).fetchJoin()
                .join(c.parentComment).fetchJoin()
                .where(where)
                .orderBy(c.createdAt.asc(), c.id.asc())
                .limit(limitPlusOne)
                .fetch();
    }

    private static long toLong(Object value) {
        if (value instanceof Number n) {
            return n.longValue();
        }
        throw new IllegalStateException("Unexpected native column type: " + value);
    }
}
