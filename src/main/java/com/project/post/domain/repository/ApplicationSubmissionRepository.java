package com.project.post.domain.repository;

import com.project.post.domain.entity.ApplicationSubmission;
import com.project.post.domain.entity.RecruitingApplication;
import com.project.post.domain.enums.Campus;
import com.project.user.domain.entity.User;
import org.springframework.data.domain.Sort;
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

    List<ApplicationSubmission> findAllByRecruitingApplicationAndDeletedAtIsNull(
            RecruitingApplication recruitingApplication,
            Sort sort
    );

    List<ApplicationSubmission> findAllByRecruitingApplicationAndCampusAndDeletedAtIsNull(
            RecruitingApplication recruitingApplication,
            Campus campus,
            Sort sort
    );

    List<ApplicationSubmission> findAllByRecruitingApplicationAndDepartment_IdAndDeletedAtIsNull(
            RecruitingApplication recruitingApplication,
            Long departmentId,
            Sort sort
    );

    List<ApplicationSubmission> findAllByRecruitingApplicationAndCampusAndDepartment_IdAndDeletedAtIsNull(
            RecruitingApplication recruitingApplication,
            Campus campus,
            Long departmentId,
            Sort sort
    );

    List<ApplicationSubmission> findAllByUserAndDeletedAtIsNullOrderBySubmittedAtDesc(User user);

    Optional<ApplicationSubmission> findByIdAndDeletedAtIsNull(Long id);
}