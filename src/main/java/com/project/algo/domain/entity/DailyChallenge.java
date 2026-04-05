package com.project.algo.domain.entity;

import com.project.algo.domain.enums.AlgorithmTag;
import com.project.algo.domain.enums.CodingTestSite;
import com.project.algo.domain.enums.Difficulty;
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
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.SQLRestriction;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "daily_challenges")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
@SQLRestriction("deleted_at IS NULL")
@AttributeOverride(name = "id", column = @Column(name = "challenge_id"))
public class DailyChallenge extends SoftDeleteEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(nullable = false, length = 300)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_site", nullable = false, length = 50)
    private CodingTestSite sourceSite;

    @Column(name = "problem_number", nullable = false, length = 50)
    private String problemNumber;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private Difficulty difficulty;

    @Builder.Default
    @ElementCollection
    @CollectionTable(
            name = "daily_challenge_algorithm_tags",
            joinColumns = @JoinColumn(name = "challenge_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "tag", length = 50)
    @BatchSize(size = 100)
    private Set<AlgorithmTag> algorithmTags = new HashSet<>();

    @Column(name = "original_url", nullable = false, columnDefinition = "TEXT")
    private String originalUrl;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "input_format", columnDefinition = "TEXT")
    private String inputFormat;

    @Column(name = "output_format", columnDefinition = "TEXT")
    private String outputFormat;

    public void update(String title, Difficulty difficulty, String description,
                       String inputFormat, String outputFormat, List<AlgorithmTag> algorithmTags) {
        if (title != null) this.title = title;
        if (difficulty != null) this.difficulty = difficulty;
        if (description != null) this.description = description;
        if (inputFormat != null) this.inputFormat = inputFormat;
        if (outputFormat != null) this.outputFormat = outputFormat;
        if (algorithmTags != null) {
            this.algorithmTags.clear();
            this.algorithmTags.addAll(algorithmTags);
        }
    }
}
