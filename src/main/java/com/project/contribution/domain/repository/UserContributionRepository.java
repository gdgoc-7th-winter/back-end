package com.project.contribution.domain.repository;

import com.project.contribution.domain.entity.UserContribution;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserContributionRepository extends JpaRepository<UserContribution, Long> {
    @Query("SELECT COUNT(uc) > 0 FROM UserContribution uc " +
            "JOIN uc.user u " +
            "JOIN uc.contributionScore cb " +
            "WHERE u.id = :userId AND cb.name = :scoreName")
    boolean existsByUserIdAndScoreName(@Param("userId") Long userId,
                                       @Param("scoreName") String scoreName);
}
