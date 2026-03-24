package com.project.post.domain.repository;

import com.project.post.domain.entity.ApplicationSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationSubmissionRepository extends JpaRepository<ApplicationSubmission, Long> {
}