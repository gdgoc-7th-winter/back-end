package com.project.post.domain.repository;

import com.project.post.domain.entity.ApplicationSubmission;
import com.project.post.domain.entity.RecruitingApplication;
import com.project.user.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface ApplicationSubmissionRepository
        extends JpaRepository<ApplicationSubmission, Long>,
        JpaSpecificationExecutor<ApplicationSubmission> {

    boolean existsByRecruitingApplicationAndUserAndDeletedAtIsNull(
            RecruitingApplication recruitingApplication,
            User user
    );

    boolean existsByRecruitingApplicationAndDeletedAtIsNull(
            RecruitingApplication recruitingApplication
    );

    List<ApplicationSubmission> findAllByUserAndDeletedAtIsNullOrderBySubmittedAtDesc(User user);

    Optional<ApplicationSubmission> findByIdAndDeletedAtIsNull(Long id);
}