package com.project.user.domain.entity;

import com.project.contribution.domain.entity.UserContribution;
import com.project.global.entity.SoftDeleteEntity;
import com.project.user.domain.enums.Authority;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.EnumType;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.FetchType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.UniqueConstraint;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(
        name = "users",
        uniqueConstraints = @UniqueConstraint(name = "uk_users_nickname", columnNames = {"nickname"})
)
@BatchSize(size = 16)
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AttributeOverride(name = "id", column = @Column(name = "user_id"))
public class User extends SoftDeleteEntity {

    // 탈퇴 시 비밀번호 치환에 사용하는 평문 표식
    public static final String WITHDRAWN_PASSWORD_PLACEHOLDER = "WITHDRAWN_ACCOUNT_NO_LOGIN";

    // 프로필 사진 S3 url
    @Column(name = "profile_img_url", columnDefinition = "TEXT")
    private String profileImgUrl;

    @Column(name = "user_email", nullable = false, unique = true)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Builder.Default
    @ElementCollection
    @CollectionTable(
            name = "user_social_account",
            joinColumns = @JoinColumn(name = "user_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"provider", "provider_id"})
    )
    private Set<SocialAccount> socialAccounts = new HashSet<>();

    @Column(name = "nickname", length = 50)
    private String nickname;

    @Column(name = "student_id", length = 20)
    private String studentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserTrack> userTracks = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserTechStack> userTechStacks = new ArrayList<>();

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "authority", nullable = false, length = 30)
    private Authority authority = Authority.DUMMY;

    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserContribution> userContributions = new ArrayList<>();

    @Builder.Default
    @Column(name = "total_point", nullable = false)
    private int totalPoint = 0;

    // 유저 레벨 뱃지 (객체 매핑)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "level_id")
    private LevelBadge levelBadge;

    @Column(name = "introduction")
    private String introduction;

    public boolean needsInitialSetup() {
        return this.department == null || this.getUserTracks().isEmpty() || this.getStudentId() == null;
    }

    public void grantUserAuthority() {
        this.authority = Authority.USER;
    }

    public void grantAuthority(Authority authority) {
        this.authority = authority;
    }

    public void updateProfile(String nickname, String studentId, Department department,
                              String profileImgUrl, String introduction,
                              List<Track> tracks, List<TechStack> techStacks) {
        if (nickname != null){
            this.nickname = nickname;
        }

        if (studentId != null){
            this.studentId = studentId;
        }

        if (department != null){
            this.department = department;
        }

        if (tracks != null) {
            this.userTracks.clear();
            tracks.forEach(this::addUserTrack);
        }

        if (profileImgUrl != null){
            this.profileImgUrl = profileImgUrl;
        }

        if (techStacks != null) {
            this.userTechStacks.clear();
            techStacks.forEach(this::addTechStack);
        }

        if (introduction != null){
            this.introduction = introduction;
        }
    }

    public void changePassword(String newPassword) {
        this.password = newPassword;
    }

    private void addUserTrack(Track trackMaster) {
        UserTrack userTrack = UserTrack.builder()
                .user(this)
                .track(trackMaster)
                .build();
        this.userTracks.add(userTrack);
    }

    private void addTechStack(TechStack techStackMaster) {
        UserTechStack userTechStack = UserTechStack.builder()
                .user(this)
                .techStack(techStackMaster)
                .build();
        this.userTechStacks.add(userTechStack);
    }

    public void initializeLevelBadge(LevelBadge initialBadge) {
        this.levelBadge = initialBadge;
    }

    public void updatePoint(int point) {
        this.totalPoint += point;
    }

    public void updateBadge(LevelBadge newBadge) {
        if (newBadge == null) {
            throw new IllegalArgumentException("뱃지 정보가 없습니다!");
        }
        this.levelBadge = newBadge;
    }

    public LevelBadge getLevelBadge() {
        if (this.levelBadge == null) return null;
        return this.levelBadge;
    }

    public void addSocialAccount(SocialAccount socialAccount) {
        boolean alreadyExists = this.socialAccounts.stream()
                .anyMatch(acc -> acc.getProvider().equals(socialAccount.getProvider())
                        && acc.getProviderId().equals(socialAccount.getProviderId()));
        if (!alreadyExists) {
            this.socialAccounts.add(socialAccount);
        }
    }

    public void withdraw() {
        softDelete();
        this.email = "deleted_" + this.getId() + "@deleted.invalid";
        this.nickname = null;
        this.studentId = null;
        this.profileImgUrl = null;
        this.introduction = null;
        this.socialAccounts.clear();
        this.userTracks.clear();
        this.userTechStacks.clear();
    }
}
