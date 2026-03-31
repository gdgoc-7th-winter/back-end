package com.project.post.domain.entity;

import jakarta.persistence.Table;
import jakarta.persistence.Entity;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.FetchType;
import jakarta.persistence.MapsId;
import jakarta.persistence.JoinColumn;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "answer_selected_options")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AnswerSelectedOption {

    @EmbeddedId
    private AnswerSelectedOptionId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("recruitingApplicationAnswerId")
    @JoinColumn(name = "recruiting_application_answer_id", nullable = false)
    private RecruitingApplicationAnswer recruitingApplicationAnswer;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("questionOptionsId")
    @JoinColumn(name = "question_options_id", nullable = false)
    private RecruitingQuestionOption questionOption;

    public AnswerSelectedOption(RecruitingApplicationAnswer recruitingApplicationAnswer,
                                RecruitingQuestionOption questionOption) {
        this.id = new AnswerSelectedOptionId(
                recruitingApplicationAnswer.getId(),
                questionOption.getId()
        );
        this.recruitingApplicationAnswer = recruitingApplicationAnswer;
        this.questionOption = questionOption;
    }
}
