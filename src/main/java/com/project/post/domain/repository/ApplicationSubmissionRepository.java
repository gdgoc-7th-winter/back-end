package com.project.post.domain.repository;

import com.project.post.domain.entity.ApplicationSubmission;
import com.project.post.domain.entity.RecruitingApplication;
import com.project.user.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ApplicationSubmissionRepository extends JpaRepository<ApplicationSubmission, Long> {
    boolean existsByRecruitingApplicationAndUserAndDeletedAtIsNull(
            RecruitingApplication recruitingApplication,
            User user
    );
    List<ApplicationSubmission> findAllByRecruitingApplicationAndDeletedAtIsNullOrderBySubmittedAtDesc(
            RecruitingApplication recruitingApplication
    );
    Optional<ApplicationSubmission> findByIdAndDeletedAtIsNull(Long id);
}
