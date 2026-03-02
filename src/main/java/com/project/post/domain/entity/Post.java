package com.project.post.domain.entity;

import com.project.global.entity.SoftDeleteEntity;
import com.project.post.domain.exception.PostDomainException;
import com.project.user.domain.entity.User;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "posts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
@AttributeOverride(name = "id", column = @Column(name = "post_id"))
@SQLRestriction("deleted_at IS NULL")
public class Post extends SoftDeleteEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "thumbnail_url", columnDefinition = "TEXT")
    private String thumbnailUrl;

    @Column(name = "view_count", nullable = false)
    @lombok.Builder.Default
    private int viewCount = 0;

    @Column(name = "like_count", nullable = false)
    @lombok.Builder.Default
    private int likeCount = 0;

    @Column(name = "scrap_count", nullable = false)
    @lombok.Builder.Default
    private int scrapCount = 0;

    @Column(name = "comment_count", nullable = false)
    @lombok.Builder.Default
    private int commentCount = 0;

    public void update(String title, String content, String thumbnailUrl) {
        if (title != null) {
            validateNotBlank(title, "제목은 공백일 수 없습니다.");
            this.title = title;
        }
        if (content != null) {
            validateNotBlank(content, "본문은 공백일 수 없습니다.");
            this.content = content;
        }
        if (thumbnailUrl != null) {
            validateNotBlank(thumbnailUrl, "썸네일 URL은 공백일 수 없습니다.");
            this.thumbnailUrl = thumbnailUrl;
        }
    }

    private void validateNotBlank(String value, String message) {
        if (value.isBlank()) {
            throw new PostDomainException(message);
        }
    }
}
