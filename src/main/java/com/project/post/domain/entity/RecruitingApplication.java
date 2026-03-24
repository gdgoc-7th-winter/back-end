package com.project.post.domain.entity;

import com.project.global.entity.AuditEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "recruiting_applications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
@AttributeOverride(name = "id", column = @Column(name = "recruiting_application_id"))
public class RecruitingApplication extends AuditEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false, unique = true)
    private RecruitingPost recruitingPost;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "message", length = 1000)
    private String message;
}