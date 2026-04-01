package com.project.contribution.infrastructure.persistence;

import com.project.contribution.domain.ContributionOutboxStatus;
import com.project.contribution.infrastructure.persistence.entity.ContributionOutboxEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

public interface ContributionOutboxJpaRepository extends JpaRepository<ContributionOutboxEntity, Long> {

    @Modifying(clearAutomatically = true)
    @Query("UPDATE ContributionOutboxEntity o SET o.status = :pending, o.lastError = :reason, o.updatedAt = :now "
            + "WHERE o.status = :processing AND o.updatedAt < :before")
    int reclaimStaleProcessing(
            @Param("pending") ContributionOutboxStatus pending,
            @Param("processing") ContributionOutboxStatus processing,
            @Param("before") Instant before,
            @Param("reason") String reason,
            @Param("now") Instant now);
}
