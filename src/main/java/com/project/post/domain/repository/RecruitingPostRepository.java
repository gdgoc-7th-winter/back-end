package com.project.post.domain.repository;

import com.project.post.domain.entity.RecruitingPost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RecruitingPostRepository extends JpaRepository<RecruitingPost, Long> {

    Optional<RecruitingPost> findActiveById(Long id);
}