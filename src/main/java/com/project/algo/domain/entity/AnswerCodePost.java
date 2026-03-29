package com.project.algo.domain.entity;

import com.project.algo.domain.enums.AlgorithmTag;
import com.project.algo.domain.enums.ProgrammingLanguage;
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
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "answer_code_posts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
@SQLRestriction("deleted_at IS NULL")
@AttributeOverride(name = "id", column = @Column(name = "answer_id"))
public class AnswerCodePost extends SoftDeleteEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_id", nullable = false)
    private DailyChallenge dailyChallenge;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Enumerated(EnumType.STRING)
    @Column(name = "language", nullable = false, length = 30)
    private ProgrammingLanguage language;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String code;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String explanation;

    @Column(name = "time_complexity", length = 100)
    private String timeComplexity;

    @Column
    private Integer runtime;

    @Builder.Default
    @ElementCollection
    @CollectionTable(
            name = "answer_code_post_algorithm_tags",
            joinColumns = @JoinColumn(name = "answer_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "tag", length = 50)
    private List<AlgorithmTag> algorithmTags = new ArrayList<>();

    @Column(name = "like_count", nullable = false)
    private long likeCount = 0L;

    public void update(ProgrammingLanguage language, String code, String explanation,
                       String timeComplexity, Integer runtime, List<AlgorithmTag> algorithmTags) {
        if (language != null) this.language = language;
        if (code != null) this.code = code;
        if (explanation != null) this.explanation = explanation;
        if (timeComplexity != null) this.timeComplexity = timeComplexity;
        if (runtime != null) this.runtime = runtime;
        if (algorithmTags != null) {
            this.algorithmTags.clear();
            this.algorithmTags.addAll(algorithmTags);
        }
    }
}
