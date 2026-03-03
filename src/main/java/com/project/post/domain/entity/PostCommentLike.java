package com.project.post.domain.entity;

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
import org.springframework.lang.NonNull;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "post_comment_likes", uniqueConstraints = {
    @UniqueConstraint(name = "uk_post_comment_likes_comment_user", columnNames = {"comment_id", "user_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostCommentLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_like_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", nullable = false)
    private PostComment comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @NonNull
    public static PostCommentLike of(@NonNull PostComment comment, @NonNull User user) {
        Objects.requireNonNull(comment, "댓글은 필수입니다.");
        Objects.requireNonNull(user, "사용자는 필수입니다.");
        PostCommentLike like = new PostCommentLike();
        like.comment = comment;
        like.user = user;
        return like;
    }
}
