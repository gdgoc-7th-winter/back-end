package com.project.algo.domain.repository;

import com.project.algo.domain.entity.DailyChallenge;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface DailyChallengeRepository extends JpaRepository<DailyChallenge, Long>, DailyChallengeRepositoryCustom {

    /** @SQLRestriction 적용으로 삭제된 항목 자동 제외 */
    Optional<DailyChallenge> findById(Long id);

    /** 상세 조회 전용 — author LAZY 필드를 한 번에 fetch */
    @EntityGraph(attributePaths = {"author"})
    Optional<DailyChallenge> findWithDetailById(Long id);

    /** MVP 선정용 — 특정 일자(UTC 범위)에 생성된 문제 전체 조회 */
    @Query("SELECT dc FROM DailyChallenge dc WHERE dc.createdAt >= :start AND dc.createdAt < :end")
    List<DailyChallenge> findAllCreatedBetween(@Param("start") Instant start, @Param("end") Instant end);
}
