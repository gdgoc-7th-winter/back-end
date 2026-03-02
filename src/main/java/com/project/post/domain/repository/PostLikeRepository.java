package com.project.post.domain.repository;

import com.project.post.domain.entity.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;

import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    @Override
    @NonNull
    <S extends PostLike> S save(@NonNull S entity);

    @Query("SELECT pl FROM PostLike pl WHERE pl.post.id = :postId AND pl.user.id = :userId")
    Optional<PostLike> findByPostIdAndUserId(@Param("postId") Long postId, @Param("userId") Long userId);

    @Query("SELECT (COUNT(pl) > 0) FROM PostLike pl WHERE pl.post.id = :postId AND pl.user.id = :userId")
    boolean existsByPostIdAndUserId(@Param("postId") Long postId, @Param("userId") Long userId);

    @Modifying
    @Query(value = "INSERT INTO post_likes (post_id, user_id, created_at) " +
            "VALUES (:postId, :userId, NOW()) " +
            "ON CONFLICT ON CONSTRAINT uk_post_likes_post_user DO NOTHING",
            nativeQuery = true)
    int insertIfAbsent(@Param("postId") Long postId, @Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM PostLike pl WHERE pl.post.id = :postId AND pl.user.id = :userId")
    int deleteByPostIdAndUserId(@Param("postId") Long postId, @Param("userId") Long userId);
}
