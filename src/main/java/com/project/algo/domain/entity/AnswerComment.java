package com.project.algo.domain.entity;

import com.project.algo.domain.enums.CommentTag;
import com.project.global.entity.SoftDeleteEntity;
import com.project.user.domain.entity.User;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "answer_comments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
@AttributeOverride(name = "id", column = @Column(name = "comment_id"))
public class AnswerComment extends SoftDeleteEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "answer_id", nullable = false)
    private AnswerCodePost answerCodePost;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    /** 소프트 삭제 시 null로 마스킹 */
    @Column(columnDefinition = "TEXT")
    private String content;

    /** 참조 코드 줄 번호 배열 (인라인 코멘트용) */
    @ElementCollection
    @CollectionTable(
            name = "answer_comment_referenced_lines",
            joinColumns = @JoinColumn(name = "comment_id")
    )
    @Column(name = "line_number")
    @Builder.Default
    private List<Integer> referencedLines = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "comment_tag", length = 50)
    private CommentTag commentTag;

    public void update(String content, CommentTag commentTag) {
        if (content != null) this.content = content;
        if (commentTag != null) this.commentTag = commentTag;
    }

    @Override
    public void softDelete() {
        super.softDelete();
        this.content = null;
    }
}
