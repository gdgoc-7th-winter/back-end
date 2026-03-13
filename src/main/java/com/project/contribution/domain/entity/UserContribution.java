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

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name="userAchievement")
@NoArgsConstructor
public class UserContribution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name= "achieveId")
    private ContributionBadge contributionBadge;
    private LocalDateTime createdAt;

    @Builder
    public UserContribution(User user, ContributionBadge contributionBadge) {
        this.user = user;
        this.contributionBadge = contributionBadge;
    }
}
