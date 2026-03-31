package com.project.post.domain.repository;

import com.project.post.domain.entity.RecruitingApplicationAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecruitingApplicationAnswerRepository extends JpaRepository<RecruitingApplicationAnswer, Long> {

    List<RecruitingApplicationAnswer> findAllByApplicationSubmissionId(Long applicationSubmissionId);

    void deleteAllByApplicationSubmissionId(Long applicationSubmissionId);
}
