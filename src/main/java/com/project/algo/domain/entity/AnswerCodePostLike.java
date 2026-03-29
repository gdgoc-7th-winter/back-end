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
import org.springframework.lang.NonNull;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(
        name = "answer_code_post_likes",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_answer_code_post_likes_answer_user",
                columnNames = {"answer_id", "user_id"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AnswerCodePostLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "like_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "answer_id", nullable = false)
    private AnswerCodePost answerCodePost;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @NonNull
    public static AnswerCodePostLike of(@NonNull AnswerCodePost answerCodePost, @NonNull User user) {
        Objects.requireNonNull(answerCodePost, "풀이 게시물은 필수입니다.");
        Objects.requireNonNull(user, "사용자는 필수입니다.");
        AnswerCodePostLike like = new AnswerCodePostLike();
        like.answerCodePost = answerCodePost;
        like.user = user;
        return like;
    }
}
