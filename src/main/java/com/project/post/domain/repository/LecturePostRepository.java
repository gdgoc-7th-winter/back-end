package com.project.post.domain.repository;

import com.project.post.domain.entity.LecturePost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LecturePostRepository extends JpaRepository<LecturePost, Long>, LecturePostRepositoryCustom {

    @Query("SELECT lp FROM LecturePost lp JOIN FETCH lp.post p WHERE lp.id = :postId AND lp.deletedAt IS NULL")
    Optional<LecturePost> findActiveById(@Param("postId") Long postId);
}
