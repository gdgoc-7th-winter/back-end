package com.project.ranking.domain.repository;

import com.project.ranking.domain.RankingPeriodType;
import com.project.ranking.domain.entity.RankingSnapshotStaging;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RankingSnapshotStagingRepository extends JpaRepository<RankingSnapshotStaging, Long> {

    @Modifying
    @Query("DELETE FROM RankingSnapshotStaging s WHERE s.periodType = :periodType AND s.periodKey = :periodKey")
    int deleteByPeriod(@Param("periodType") RankingPeriodType periodType, @Param("periodKey") String periodKey);
}
