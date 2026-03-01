package com.project.user.domain.entity;
import com.project.user.domain.enums.Authority;
import com.project.user.domain.enums.Interest;
import com.project.user.domain.enums.TechStack;
import com.project.user.domain.enums.Track;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.EnumType;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.JoinColumn;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="user_id")
    private Long id;

    @Column(name="email",nullable = false, unique = true)
    private String email;

    @Column(name = "password",nullable = false)
    private String password;

    @Column(name="nickname", length = 50)
    private String nickname;

    @Column(name = "student_id", length = 20)
    private String studentId;

    @Column(name = "department", length = 50)
    private String department;


    @Enumerated(EnumType.STRING)
    @Column(name = "track")
    private Track track;

    @Column(name = "user_badge", nullable = false)
    private String userBadge = "DUMMY";

    // 프로필 사진 S3 url
    @Column(name = "profile_img_url", columnDefinition = "TEXT")
    private String profileImgUrl;

    // 권한 레벨 (Dummy, User, Manager, Admin)
    @Enumerated(EnumType.STRING)
    @Column(name="authority", nullable = false, length = 30)
    private Authority authority = Authority.DUMMY;

    // 회원가입 시점
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    //프로필 정보 업데이트 시각 반영
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    // 기술 스택: 복수 선택 (별도 테이블 user_tech_stacks 생성)
    @ElementCollection(targetClass = TechStack.class)
    @CollectionTable(name = "user_tech_stacks", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "stack_name")
    private Set<TechStack> techStacks = new HashSet<>();

    // 관심사: 복수 선택 (별도 테이블 user_interests_enum 생성)
    @ElementCollection(targetClass = Interest.class)
    @CollectionTable(name = "user_interests_enum", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "interest_name")
    private Set<Interest> interests = new HashSet<>();

    @Builder
    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public boolean needsInitialSetup() {
        return this.authority == Authority.DUMMY;
    }

    public void promoteToUser() {
        this.authority = Authority.USER;
    }

    public void updateProfile(String nickname, String studentId, String department,
                              Track track, String profileImgUrl, Set<TechStack> techStacks, Set<Interest> interests) {
        this.nickname = nickname;
        this.studentId = studentId;
        this.department = department;
        this.track = track;
        this.profileImgUrl = profileImgUrl;

        this.techStacks.clear();
        if (techStacks != null) this.techStacks.addAll(techStacks);

        this.interests.clear();
        if (interests != null) this.interests.addAll(interests);

        this.authority = Authority.USER;
        this.updatedAt = OffsetDateTime.now();
    }
}
