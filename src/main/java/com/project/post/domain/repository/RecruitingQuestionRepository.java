package com.project.post.domain.repository;

import com.project.post.domain.entity.RecruitingQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecruitingQuestionRepository extends JpaRepository<RecruitingQuestion, Long> {

    List<RecruitingQuestion> findAllByRecruitingApplicationIdOrderBySortOrderAsc(Long recruitingApplicationId);

}
