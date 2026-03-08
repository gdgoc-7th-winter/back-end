package com.project.post.domain.entity;

import com.project.post.domain.enums.PromotionCategory;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "promotion_posts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
public class PromotionPost {

    @Id
    @Column(name = "post_id")
    private Long id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(name = "category", nullable = false)
    @Enumerated(EnumType.STRING)
    private PromotionCategory category;
}