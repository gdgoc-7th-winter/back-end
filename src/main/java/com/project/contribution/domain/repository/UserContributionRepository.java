package com.project.contribution.domain.repository;

import com.project.contribution.domain.entity.UserContribution;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserContributionRepository extends JpaRepository<UserContribution, Long> {

    boolean existsByIdempotencyKey(String idempotencyKey);
}
