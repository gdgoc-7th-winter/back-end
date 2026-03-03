package com.project.post.domain.repository;

import com.project.post.domain.entity.PostCommentLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;

import java.util.Optional;

public interface PostCommentLikeRepository extends JpaRepository<PostCommentLike, Long> {

    @Override
    @NonNull
    <S extends PostCommentLike> S save(@NonNull S entity);

    @Query("SELECT pcl FROM PostCommentLike pcl WHERE pcl.comment.id = :commentId AND pcl.user.id = :userId")
    Optional<PostCommentLike> findByCommentIdAndUserId(@Param("commentId") Long commentId, @Param("userId") Long userId);

    @Query("SELECT (COUNT(pcl) > 0) FROM PostCommentLike pcl WHERE pcl.comment.id = :commentId AND pcl.user.id = :userId")
    boolean existsByCommentIdAndUserId(@Param("commentId") Long commentId, @Param("userId") Long userId);

    @Modifying
    @Query(value = "INSERT INTO post_comment_likes (comment_id, user_id, created_at) " +
            "VALUES (:commentId, :userId, NOW()) " +
            "ON CONFLICT ON CONSTRAINT uk_post_comment_likes_comment_user DO NOTHING",
            nativeQuery = true)
    int insertIfAbsent(@Param("commentId") Long commentId, @Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM PostCommentLike pcl WHERE pcl.comment.id = :commentId AND pcl.user.id = :userId")
    int deleteByCommentIdAndUserId(@Param("commentId") Long commentId, @Param("userId") Long userId);
}
