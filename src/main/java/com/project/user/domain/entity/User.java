package com.project.user.domain.entity;

import com.project.contribution.domain.entity.UserContribution;
import com.project.user.domain.enums.Authority;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GenerationType;
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

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "users")
@BatchSize(size = 16)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="user_id")
    private Long id;

    // 프로필 사진 S3 url
    @Column(name = "profile_img_url", columnDefinition = "TEXT")
    private String profileImgUrl;

    @Column(name = "user_email", nullable = false, unique = true)
    private String email;

    @Column(name = "password",nullable = false)
    private String password;

    @ElementCollection
    @CollectionTable(
            name = "user_social_account",
            joinColumns = @JoinColumn(name = "user_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"provider", "provider_id"})
    )
    private Set<SocialAccount> socialAccounts = new HashSet<>();

    @Column(name="nickname", length = 50)
    private String nickname;

    @Column(name = "student_id", length = 20)
    private String studentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserTrack> userTracks = new ArrayList<>();

    // 2. 기술 스택 정보 (ElementCollection 대신 UserTechStack 엔티티 리스트로 관리)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserTechStack> userTechStacks = new ArrayList<>();

    // 권한 레벨 (Dummy, User, Manager, Admin)
    @Enumerated(EnumType.STRING)
    @Column(name="authority", nullable = false, length = 30)
    private Authority authority = Authority.DUMMY;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserContribution> userContributions = new ArrayList<>();

    @Column(name = "total_point", nullable = false)
    private int totalPoint = 0;

    // 유저 레벨 뱃지 (객체 매핑)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="level_id")
    private LevelBadge levelBadge;

    // 회원가입 시점
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    //프로필 정보 업데이트 시각 반영
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "introduction", nullable = true)
    private String introduction;

    @Builder
    public User(String email, String password, String nickname) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
    }

    public boolean needsInitialSetup() {
        return this.department == null || this.getUserTracks().isEmpty() || this.getStudentId() == null;
    }

    public void grantUserAuthority() {
        this.authority = Authority.USER;
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
            tracks.forEach(this::addUserTrack); // addUserTrack(Track track) 메서드 호출
        }

        if (profileImgUrl != null){
            this.profileImgUrl = profileImgUrl;
        }

        if (techStacks != null) {
            this.userTechStacks.clear();
            techStacks.forEach(this::addTechStack); // addTechStack(TechStack techStack) 호출
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
}
