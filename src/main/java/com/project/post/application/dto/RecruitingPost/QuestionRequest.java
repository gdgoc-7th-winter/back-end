package com.project.post.application.dto.RecruitingPost;

import com.project.post.domain.enums.RecruitingQuestionType;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class QuestionRequest {

    private String content;
    private RecruitingQuestionType type;
    private boolean required;
    private int sortOrder;

    private List<QuestionOptionRequest> options; // 선택형일 때만 사용
}
