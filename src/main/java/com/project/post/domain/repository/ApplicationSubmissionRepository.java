package com.project.post.domain.repository;

import com.project.post.domain.entity.ApplicationSubmission;
import com.project.post.domain.entity.RecruitingApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ApplicationSubmissionRepository extends JpaRepository<ApplicationSubmission, Long> {

    Optional<ApplicationSubmission> findByIdAndUserId(Long id, Long userId);
    List<ApplicationSubmission> findAllByRecruitingApplicationOrderBySubmittedAtDesc(RecruitingApplication recruitingApplication);
}