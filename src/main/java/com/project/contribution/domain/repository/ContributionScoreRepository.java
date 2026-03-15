package com.project.contribution.domain.repository;

import com.project.contribution.domain.entity.ContributionScore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ContributionScoreRepository extends JpaRepository<ContributionScore, Long> {
    Optional<ContributionScore> findByName(String name);
}
