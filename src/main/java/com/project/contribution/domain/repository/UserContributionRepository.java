package com.project.contribution.domain.repository;

import com.project.contribution.domain.entity.UserContribution;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserContributionRepository extends JpaRepository<UserContribution, String> {
    @Query("SELECT COUNT(uc) > 0 FROM UserContribution uc " +
            "JOIN uc.user u " +
            "JOIN uc.contributionBadge cb " +
            "WHERE u.id = :userId AND cb.name = :badgeName")
    boolean existsByUserIdAndBadgeName(@Param("userId") Long userId,
                                       @Param("badgeName") String badgeName);
}
