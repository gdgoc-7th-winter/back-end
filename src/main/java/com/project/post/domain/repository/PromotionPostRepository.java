package com.project.post.domain.repository;

import com.project.post.domain.entity.PromotionPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PromotionPostRepository extends JpaRepository<PromotionPost, Long>, PromotionPostRepositoryCustom {

    @Query("SELECT pp FROM PromotionPost pp JOIN FETCH pp.post p WHERE pp.id = :postId AND pp.deletedAt IS NULL AND p.deletedAt IS NULL")
    Optional<PromotionPost> findActiveById(@Param("postId") Long postId);
}
