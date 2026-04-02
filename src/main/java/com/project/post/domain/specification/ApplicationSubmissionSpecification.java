package com.project.post.domain.specification;

import com.project.post.domain.entity.ApplicationSubmission;
import com.project.post.domain.entity.RecruitingApplication;
import com.project.post.domain.enums.Campus;
import org.springframework.data.jpa.domain.Specification;

public class ApplicationSubmissionSpecification {

    public static Specification<ApplicationSubmission> hasRecruitingApplication(
            RecruitingApplication recruitingApplication
    ) {
        return (root, query, cb) ->
                cb.equal(root.get("recruitingApplication"), recruitingApplication);
    }

    public static Specification<ApplicationSubmission> isNotDeleted() {
        return (root, query, cb) ->
                cb.isNull(root.get("deletedAt"));
    }

    public static Specification<ApplicationSubmission> hasCampus(Campus campus) {
        return (root, query, cb) ->
                campus == null ? null : cb.equal(root.get("campus"), campus);
    }

    public static Specification<ApplicationSubmission> hasDepartmentId(Long departmentId) {
        return (root, query, cb) ->
                departmentId == null ? null : cb.equal(root.get("department").get("id"), departmentId);
    }

    public static Specification<ApplicationSubmission> applicantNameContains(String applicantName) {
        return (root, query, cb) ->
                (applicantName == null || applicantName.isBlank())
                        ? null
                        : cb.like(
                        cb.lower(root.get("applicantName")),
                        "%" + applicantName.toLowerCase() + "%"
                );
    }
}
