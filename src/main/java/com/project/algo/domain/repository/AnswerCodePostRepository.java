package com.project.algo.domain.repository;

import com.project.algo.domain.entity.AnswerCodePost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AnswerCodePostRepository extends JpaRepository<AnswerCodePost, Long> {

    Page<AnswerCodePost> findByDailyChallengeId(Long challengeId, Pageable pageable);

    Optional<AnswerCodePost> findByDailyChallengeIdAndAuthorId(Long challengeId, Long authorId);

    boolean existsByDailyChallengeIdAndAuthorId(Long challengeId, Long authorId);

    /**
     * MVP 선정용 — 좋아요 내림차순, 동점 시 먼저 제출한 순으로 상위 3개 반환.
     * @SQLRestriction 으로 소프트 삭제된 풀이는 자동 제외.
     */
    @Query("SELECT a FROM AnswerCodePost a WHERE a.dailyChallenge.id = :challengeId ORDER BY a.likeCount DESC, a.createdAt ASC LIMIT 3")
    List<AnswerCodePost> findTop3ForMvp(@Param("challengeId") Long challengeId);

    @Modifying
    @Query("UPDATE AnswerCodePost a SET a.likeCount = a.likeCount + 1 WHERE a.id = :id")
    int incrementLikeCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE AnswerCodePost a SET a.likeCount = GREATEST(a.likeCount - 1, 0) WHERE a.id = :id")
    int decrementLikeCount(@Param("id") Long id);
}
