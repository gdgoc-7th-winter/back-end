package com.project.contribution.domain.entity;

import com.project.global.entity.AuditEntity;
import com.project.user.domain.entity.User;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Column;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "user_contribution",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "UK_USER_CONTRIBUTION_USER_SCORE",
                        columnNames = {"user_id", "contribution_id", "reference_id"}
                )
        }
)
public class UserContribution extends AuditEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contribution_id", nullable = false)
    private ContributionScore contributionScore;

    @Column(nullable = false, name = "reference_id")
    private Long referenceId;

    public UserContribution(User user, ContributionScore contributionScore, Long referenceId) {
        this.user = user;
        this.contributionScore = contributionScore;
        this.referenceId = referenceId;
    }
}
