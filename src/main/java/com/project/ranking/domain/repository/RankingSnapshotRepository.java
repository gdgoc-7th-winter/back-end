package com.project.ranking.domain.repository;

import com.project.ranking.domain.RankingPeriodType;
import com.project.ranking.domain.entity.RankingSnapshot;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface RankingSnapshotRepository extends JpaRepository<RankingSnapshot, Long> {

    @Modifying
    @Query("DELETE FROM RankingSnapshot r WHERE r.periodType = :periodType AND r.periodKey = :periodKey")
    int deleteByPeriod(@Param("periodType") RankingPeriodType periodType, @Param("periodKey") String periodKey);

    @Query("SELECT MAX(r.calculatedAt) FROM RankingSnapshot r WHERE r.periodType = :periodType AND r.periodKey = :periodKey")
    Optional<Instant> findMaxCalculatedAt(
            @Param("periodType") RankingPeriodType periodType,
            @Param("periodKey") String periodKey);
}
