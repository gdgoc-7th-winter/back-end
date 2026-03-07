package com.project.post.domain.repository;

import com.project.post.domain.entity.PromotionPost;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PromotionPostRepository extends JpaRepository<PromotionPost, Long> {
}