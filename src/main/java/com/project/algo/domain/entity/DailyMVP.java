package com.project.algo.domain.entity;

import com.project.user.domain.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(
        name = "daily_mvps",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_daily_mvps_challenge_user", columnNames = {"challenge_id", "user_id"}),
                @UniqueConstraint(name = "uk_daily_mvps_challenge_rank", columnNames = {"challenge_id", "rank"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyMVP {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mvp_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_id", nullable = false)
    private DailyChallenge dailyChallenge;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** 1 ~ 3위 */
    @Column(nullable = false)
    private int rank;

    @Column(name = "like_count", nullable = false)
    private long likeCount;

    @Column(name = "awarded_at", nullable = false)
    private LocalDate awardedAt;

    public static DailyMVP of(DailyChallenge dailyChallenge, User user,
                               int rank, long likeCount, LocalDate awardedAt) {
        DailyMVP mvp = new DailyMVP();
        mvp.dailyChallenge = dailyChallenge;
        mvp.user = user;
        mvp.rank = rank;
        mvp.likeCount = likeCount;
        mvp.awardedAt = awardedAt;
        return mvp;
    }
}
