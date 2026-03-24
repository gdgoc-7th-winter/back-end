package com.project.post.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class AnswerSelectedOptionId implements Serializable {

    @Column(name = "recruiting_application_answer_id")
    private Long recruitingApplicationAnswerId;

    @Column(name = "question_options_id")
    private Long questionOptionsId;

    public AnswerSelectedOptionId(Long recruitingApplicationAnswerId, Long questionOptionsId) {
        this.recruitingApplicationAnswerId = recruitingApplicationAnswerId;
        this.questionOptionsId = questionOptionsId;
    }
}