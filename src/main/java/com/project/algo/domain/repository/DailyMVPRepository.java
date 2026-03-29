package com.project.algo.domain.repository;

import com.project.algo.domain.entity.DailyMVP;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyMVPRepository extends JpaRepository<DailyMVP, Long> {

    List<DailyMVP> findByDailyChallengeIdOrderByRankAsc(Long challengeId);

    /** 전일 rank 1 MVP 조회 → DailyChallenge 등록 권한 확인용 */
    Optional<DailyMVP> findByUserIdAndAwardedAtAndRank(Long userId, LocalDate awardedAt, int rank);

    boolean existsByUserIdAndAwardedAtAndRank(Long userId, LocalDate awardedAt, int rank);

    /** MVP 선정 재실행 시 기존 데이터 삭제 (멱등성 보장) */
    void deleteByDailyChallengeIdAndAwardedAt(Long challengeId, LocalDate awardedAt);
}
