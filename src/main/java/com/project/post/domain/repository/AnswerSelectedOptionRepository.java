package com.project.post.domain.repository;

import com.project.post.domain.entity.AnswerSelectedOption;
import com.project.post.domain.entity.AnswerSelectedOptionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AnswerSelectedOptionRepository extends JpaRepository<AnswerSelectedOption, AnswerSelectedOptionId> {

    @Modifying
    @Query("""
            delete from AnswerSelectedOption a
            where a.recruitingApplicationAnswer.applicationSubmission.id = :applicationSubmissionId
            """)
    int deleteByApplicationSubmissionId(@Param("applicationSubmissionId") Long applicationSubmissionId);

    List<AnswerSelectedOption> findAllByRecruitingApplicationAnswerId(Long recruitingApplicationAnswerId);
    List<AnswerSelectedOption> findAllByRecruitingApplicationAnswerIdIn(List<Long> answerIds);
}
