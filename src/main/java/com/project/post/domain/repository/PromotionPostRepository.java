package com.project.post.domain.repository;

import com.project.post.domain.entity.PromotionPost;
import com.project.post.domain.enums.PromotionCategory;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PromotionPostRepository extends JpaRepository<PromotionPost, Long> {

    Optional<PromotionPost> findByIdAndDeletedAtIsNull(Long id);

    Page<PromotionPost> findByCategoryAndDeletedAtIsNull(PromotionCategory category, Pageable pageable);
    Page<PromotionPost> findByDeletedAtIsNull(Pageable pageable);
    List<PromotionPost> findByCategoryAndDeletedAtIsNull(PromotionCategory category);
}
