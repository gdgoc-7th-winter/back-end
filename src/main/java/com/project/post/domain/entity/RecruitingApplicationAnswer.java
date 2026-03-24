package com.project.post.domain.entity;

import com.project.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "recruiting_application_answers")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
@AttributeOverride(name = "id", column = @Column(name = "recruiting_application_answer_id"))
public class RecruitingApplicationAnswer extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_submission_id", nullable = false)
    private ApplicationSubmission applicationSubmission;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recruiting_questions_id", nullable = false)
    private RecruitingQuestion question;

    @Column(name = "answer", columnDefinition = "TEXT")
    private String answer;
}