package com.project.algo.domain.repository;

import com.project.algo.domain.entity.AnswerComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnswerCommentRepository extends JpaRepository<AnswerComment, Long> {

    Page<AnswerComment> findByAnswerCodePostId(Long answerId, Pageable pageable);
}
