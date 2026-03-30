package com.project.post.domain.repository;

import com.project.post.domain.entity.ApplicationSubmission;
import com.project.post.domain.entity.RecruitingApplication;
import com.project.post.domain.enums.Campus;
import com.project.user.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ApplicationSubmissionRepository extends JpaRepository<ApplicationSubmission, Long> {

    boolean existsByRecruitingApplicationAndUserAndDeletedAtIsNull(
            RecruitingApplication recruitingApplication,
            User user
    );

    boolean existsByRecruitingApplicationAndDeletedAtIsNull(
            RecruitingApplication recruitingApplication
    );

    List<ApplicationSubmission> findAllByRecruitingApplicationAndDeletedAtIsNullOrderBySubmittedAtDesc(
            RecruitingApplication recruitingApplication
    );

    List<ApplicationSubmission> findAllByUserAndDeletedAtIsNullOrderBySubmittedAtDesc(User user);

    Optional<ApplicationSubmission> findByIdAndDeletedAtIsNull(Long id);

    List<ApplicationSubmission> findAllByRecruitingApplicationAndCampusAndDepartment_IdAndDeletedAtIsNullOrderBySubmittedAtDesc(
            RecruitingApplication recruitingApplication,
            Campus campus,
            Long departmentId
    );

    List<ApplicationSubmission> findAllByRecruitingApplicationAndCampusAndDeletedAtIsNullOrderBySubmittedAtDesc(
            RecruitingApplication recruitingApplication,
            Campus campus
    );

    List<ApplicationSubmission> findAllByRecruitingApplicationAndDepartment_IdAndDeletedAtIsNullOrderBySubmittedAtDesc(
            RecruitingApplication recruitingApplication,
            Long departmentId
    );
}