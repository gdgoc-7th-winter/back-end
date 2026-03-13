package com.project.contribution.domain.repository;

import com.project.contribution.domain.entity.ContributionBadge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ContributionBadgeRepository extends JpaRepository<ContributionBadge, Long> {
    Optional<ContributionBadge> findByName(String name);
}
