package com.project.contribution.domain.entity;

import com.project.user.domain.entity.User;
import jakarta.persistence.Table;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.FetchType;
import jakarta.persistence.UniqueConstraint;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(
        name = "userAchievement",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "UK_USER_CONTRIBUTION_USER_SCORE",
                        columnNames = {"user_id", "contributionId"}
                )
        }
)
@NoArgsConstructor
public class UserContribution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name= "contributionId")
    private ContributionScore contributionScore;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @Builder
    public UserContribution(User user, ContributionScore contributionScore) {
        this.user = user;
        this.contributionScore = contributionScore;
    }
}
