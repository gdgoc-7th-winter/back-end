package com.project.post.domain.entity;

import com.project.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "recruiting_question_options")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
@AttributeOverride(name = "id", column = @Column(name = "question_options_id"))
public class RecruitingQuestionOption extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recruiting_questions_id", nullable = false)
    private RecruitingQuestion question;

    @Column(name = "label", nullable = false, length = 100)
    private String label;
}