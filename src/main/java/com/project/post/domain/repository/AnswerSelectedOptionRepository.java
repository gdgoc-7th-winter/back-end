package com.project.post.domain.repository;

import com.project.post.domain.entity.AnswerSelectedOption;
import com.project.post.domain.entity.AnswerSelectedOptionId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnswerSelectedOptionRepository extends JpaRepository<AnswerSelectedOption, AnswerSelectedOptionId> {
    void deleteAllByRecruitingApplicationAnswerApplicationSubmissionId(Long applicationSubmissionId);
}