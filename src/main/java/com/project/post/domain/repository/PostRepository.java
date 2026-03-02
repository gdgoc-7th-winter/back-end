package com.project.post.domain.repository;

import com.project.post.domain.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;

import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {

    @Override
    @NonNull
    <S extends Post> S save(@NonNull S entity);

    @Query("SELECT p FROM Post p WHERE p.id = :postId AND p.deletedAt IS NULL")
    Optional<Post> findActiveById(@Param("postId") Long postId);

    @Query("SELECT (COUNT(p) > 0) FROM Post p WHERE p.id = :postId AND p.deletedAt IS NULL")
    boolean existsActiveById(@Param("postId") Long postId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Post p WHERE p.id = :postId AND p.deletedAt IS NULL")
    Optional<Post> findByIdForUpdate(@Param("postId") Long postId);

    @Modifying
    @Query("UPDATE Post p SET p.viewCount = p.viewCount + 1 WHERE p.id = :postId AND p.deletedAt IS NULL")
    int incrementViewCount(@Param("postId") Long postId);

    @Modifying
    @Query("UPDATE Post p SET p.likeCount = p.likeCount + 1 WHERE p.id = :postId AND p.deletedAt IS NULL")
    int incrementLikeCount(@Param("postId") Long postId);

    @Modifying
    @Query("UPDATE Post p SET p.likeCount = CASE WHEN p.likeCount > 0 THEN p.likeCount - 1 ELSE 0 END WHERE p.id = :postId AND p.deletedAt IS NULL")
    int decrementLikeCount(@Param("postId") Long postId);

    @Query("SELECT p.likeCount FROM Post p WHERE p.id = :postId AND p.deletedAt IS NULL")
    Optional<Integer> findLikeCountById(@Param("postId") Long postId);

    @Modifying
    @Query("UPDATE Post p SET p.scrapCount = p.scrapCount + 1 WHERE p.id = :postId AND p.deletedAt IS NULL")
    int incrementScrapCount(@Param("postId") Long postId);

    @Modifying
    @Query("UPDATE Post p SET p.scrapCount = CASE WHEN p.scrapCount > 0 THEN p.scrapCount - 1 ELSE 0 END WHERE p.id = :postId AND p.deletedAt IS NULL")
    int decrementScrapCount(@Param("postId") Long postId);

    @Query("SELECT p.scrapCount FROM Post p WHERE p.id = :postId AND p.deletedAt IS NULL")
    Optional<Integer> findScrapCountById(@Param("postId") Long postId);
}
