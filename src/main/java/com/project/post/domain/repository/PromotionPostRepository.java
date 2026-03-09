package com.project.post.domain.repository;

import com.project.post.domain.entity.PromotionPost;
import com.project.post.domain.enums.PromotionCategory;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PromotionPostRepository extends JpaRepository<PromotionPost, Long> {

    Page<PromotionPost> findByCategory(PromotionCategory category, Pageable pageable);
    List<PromotionPost> findByCategory(PromotionCategory category);

}