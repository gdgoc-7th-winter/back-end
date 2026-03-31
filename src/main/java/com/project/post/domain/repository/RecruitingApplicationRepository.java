package com.project.post.domain.repository;

import com.project.post.domain.entity.RecruitingApplication;
import com.project.post.domain.entity.RecruitingPost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RecruitingApplicationRepository extends JpaRepository<RecruitingApplication, Long> {

    Optional<RecruitingApplication> findByRecruitingPost(RecruitingPost recruitingPost);

    Optional<RecruitingApplication> findByRecruitingPostId(Long recruitingPostId);
}
