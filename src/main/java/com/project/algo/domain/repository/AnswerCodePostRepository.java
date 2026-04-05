package com.project.algo.domain.repository;

import com.project.algo.domain.entity.AnswerCodePost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface AnswerCodePostRepository extends JpaRepository<AnswerCodePost, Long> {

    Page<AnswerCodePost> findByDailyChallengeId(Long challengeId, Pageable pageable);

    /** 상세 조회 전용 — dailyChallenge·author LAZY 필드를 한 번에 fetch */
    @EntityGraph(attributePaths = {"dailyChallenge", "author"})
    Optional<AnswerCodePost> findWithDetailById(Long id);

    Optional<AnswerCodePost> findByDailyChallengeIdAndAuthorId(Long challengeId, Long authorId);

    boolean existsByDailyChallengeIdAndAuthorId(Long challengeId, Long authorId);

    /**
     * MVP 선정용 — 특정 날짜(KST)에 제출된 모든 풀이를 좋아요 내림차순, 동점 시 먼저 제출한 순으로 상위 N개 반환.
     * challenge 구분 없이 전체 범위에서 선정한다.
     * @SQLRestriction 으로 소프트 삭제된 풀이는 자동 제외.
     */
    @Query("SELECT a FROM AnswerCodePost a JOIN FETCH a.author JOIN FETCH a.dailyChallenge WHERE a.createdAt >= :start AND a.createdAt < :end")
    List<AnswerCodePost> findTop3ForMvp(@Param("start") Instant start, @Param("end") Instant end, Pageable pageable);

    @Modifying
    @Query("UPDATE AnswerCodePost a SET a.likeCount = a.likeCount + 1 WHERE a.id = :id")
    int incrementLikeCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE AnswerCodePost a SET a.likeCount = CASE WHEN a.likeCount > 0 THEN a.likeCount - 1 ELSE 0 END WHERE a.id = :id")
    int decrementLikeCount(@Param("id") Long id);
}
