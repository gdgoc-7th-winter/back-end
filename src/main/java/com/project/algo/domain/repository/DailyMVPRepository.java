package com.project.algo.domain.repository;

import com.project.algo.domain.entity.DailyMVP;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyMVPRepository extends JpaRepository<DailyMVP, Long> {

    /** 상세 조회 전용 — user LAZY 필드를 한 번에 fetch */
    @Query("SELECT m FROM DailyMVP m JOIN FETCH m.user WHERE m.dailyChallenge.id = :challengeId ORDER BY m.rank ASC")
    List<DailyMVP> findByDailyChallengeIdOrderByRankAsc(@Param("challengeId") Long challengeId);

    /** 전일 rank 1 MVP 조회 → DailyChallenge 등록 권한 확인용 */
    Optional<DailyMVP> findByUserIdAndAwardedAtAndRank(Long userId, LocalDate awardedAt, int rank);

    boolean existsByUserIdAndAwardedAtAndRank(Long userId, LocalDate awardedAt, int rank);

    /** MVP 선정 재실행 시 기존 데이터 삭제 (멱등성 보장) */
    @Modifying
    void deleteByDailyChallengeIdAndAwardedAt(Long challengeId, LocalDate awardedAt);

    /** 날짜 단위 전체 삭제 — challenge 무관 MVP 선정 재실행 시 사용 */
    @Modifying
    void deleteByAwardedAt(LocalDate awardedAt);
}
