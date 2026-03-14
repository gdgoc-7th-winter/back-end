package com.project.contribution.domain.entity;

import com.project.user.domain.entity.User;

import jakarta.persistence.Id;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.FetchType;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.GenerationType;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(
        name = "userContribution",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "UK_USER_CONTRIBUTION_USER_SCORE",
                        columnNames = {"user_id", "contributionId", "referenceId"}
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
    @JoinColumn(name= "contribution_id")
    private ContributionScore contributionScore;

    @Column(nullable = false, name="reference_id")
    private Long referenceId;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @Builder
    public UserContribution(User user, ContributionScore contributionScore,  Long referenceId) {
        this.user = user;
        this.contributionScore = contributionScore;
        this.referenceId = referenceId;
    }
}
