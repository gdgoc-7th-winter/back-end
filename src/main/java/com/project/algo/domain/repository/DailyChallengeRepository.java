package com.project.algo.domain.repository;

import com.project.algo.domain.entity.DailyChallenge;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface DailyChallengeRepository extends JpaRepository<DailyChallenge, Long>, DailyChallengeRepositoryCustom {

    /** @SQLRestriction 적용으로 삭제된 항목 자동 제외 */
    Optional<DailyChallenge> findById(Long id);

    /** MVP 선정용 — 특정 일자(UTC 범위)에 생성된 문제 전체 조회 */
    @Query("SELECT dc FROM DailyChallenge dc WHERE dc.createdAt >= :start AND dc.createdAt < :end")
    List<DailyChallenge> findAllCreatedBetween(@Param("start") Instant start, @Param("end") Instant end);

    @Query("""
            SELECT DISTINCT dc FROM DailyChallenge dc
            LEFT JOIN dc.algorithmTags t
            WHERE (:keyword IS NULL
                   OR LOWER(dc.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(dc.problemNumber) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:#{#tags == null || #tags.isEmpty()} = true OR t IN :tags)
            """)
    Page<DailyChallenge> findByKeywordAndTags(
            @Param("keyword") String keyword,
            @Param("tags") List<String> tags,
            Pageable pageable
    );
}
