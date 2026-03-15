package com.project.post.domain.repository;

import com.project.post.domain.entity.PromotionPost;
import com.project.post.domain.enums.PromotionCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PromotionPostRepository extends JpaRepository<PromotionPost, Long> {

    @Query("SELECT pp FROM PromotionPost pp JOIN FETCH pp.post p WHERE pp.id = :postId AND pp.deletedAt IS NULL")
    Optional<PromotionPost> findActiveById(@Param("postId") Long postId);

    @Query("SELECT pp FROM PromotionPost pp WHERE pp.category = :category")
    @EntityGraph(attributePaths = {"post", "post.author"})
    Page<PromotionPost> findAllActiveByCategory(@Param("category") PromotionCategory category, Pageable pageable);

    @Query("SELECT pp FROM PromotionPost pp")
    @EntityGraph(attributePaths = {"post", "post.author"})
    Page<PromotionPost> findAllActive(Pageable pageable);

    @Query("SELECT pp FROM PromotionPost pp WHERE pp.category = :category")
    List<PromotionPost> findAllActiveByCategory(@Param("category") PromotionCategory category);
}
