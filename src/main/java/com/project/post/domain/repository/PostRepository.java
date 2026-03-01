package com.project.post.domain.repository;

import com.project.post.domain.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {

    @Query("SELECT p FROM Post p WHERE p.id = :postId AND p.deletedAt IS NULL")
    Optional<Post> findActiveById(@Param("postId") Long postId);

    @Query("SELECT (COUNT(p) > 0) FROM Post p WHERE p.id = :postId AND p.deletedAt IS NULL")
    boolean existsActiveById(@Param("postId") Long postId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Post p WHERE p.id = :postId AND p.deletedAt IS NULL")
    Optional<Post> findByIdForUpdate(@Param("postId") Long postId);
}
