package com.project.algo.domain.repository;

import com.project.algo.domain.entity.AnswerCodePostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AnswerCodePostLikeRepository extends JpaRepository<AnswerCodePostLike, Long> {

    @Modifying
    @Query("DELETE FROM AnswerCodePostLike l WHERE l.answerCodePost.id = :answerId AND l.user.id = :userId")
    int deleteByAnswerCodePostIdAndUserId(@Param("answerId") Long answerId, @Param("userId") Long userId);
}
