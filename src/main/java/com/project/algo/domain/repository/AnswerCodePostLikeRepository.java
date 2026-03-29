package com.project.algo.domain.repository;

import com.project.algo.domain.entity.AnswerCodePostLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AnswerCodePostLikeRepository extends JpaRepository<AnswerCodePostLike, Long> {

    Optional<AnswerCodePostLike> findByAnswerCodePostIdAndUserId(Long answerId, Long userId);

    boolean existsByAnswerCodePostIdAndUserId(Long answerId, Long userId);
}
