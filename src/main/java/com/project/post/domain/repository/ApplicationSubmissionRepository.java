package com.project.post.domain.repository;

import com.project.post.domain.entity.ApplicationSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApplicationSubmissionRepository extends JpaRepository<ApplicationSubmission, Long> {

    Optional<ApplicationSubmission> findByIdAndUserId(Long id, Long userId);
}