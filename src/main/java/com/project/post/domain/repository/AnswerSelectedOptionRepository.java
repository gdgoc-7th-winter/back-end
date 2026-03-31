package com.project.post.domain.repository;

import com.project.post.domain.entity.AnswerSelectedOption;
import com.project.post.domain.entity.AnswerSelectedOptionId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnswerSelectedOptionRepository extends JpaRepository<AnswerSelectedOption, AnswerSelectedOptionId> {

    void deleteAllByRecruitingApplicationAnswerApplicationSubmissionId(Long applicationSubmissionId);

    List<AnswerSelectedOption> findAllByRecruitingApplicationAnswerId(Long recruitingApplicationAnswerId);
    List<AnswerSelectedOption> findAllByRecruitingApplicationAnswerIdIn(List<Long> answerIds);
}
