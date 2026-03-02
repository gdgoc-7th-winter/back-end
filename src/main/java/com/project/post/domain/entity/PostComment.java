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

import java.util.Objects;

@Entity
@Table(name = "post_comments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
@AttributeOverride(name = "id", column = @Column(name = "comment_id"))
public class PostComment extends SoftDeleteEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private PostComment parentComment;

    @Column(name = "depth", nullable = false)
    @lombok.Builder.Default
    private int depth = 0;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "like_count", nullable = false)
    @lombok.Builder.Default
    private long likeCount = 0;

    @Override
    public void softDelete() {
        super.softDelete();
        this.content = null;
    }

    public static PostComment createRoot(Post post, User user, String content) {
        Objects.requireNonNull(post, "게시글은 필수입니다.");
        Objects.requireNonNull(user, "사용자는 필수입니다.");
        Objects.requireNonNull(content, "댓글 내용은 필수입니다.");
        return PostComment.builder()
                .post(post)
                .user(user)
                .parentComment(null)
                .depth(0)
                .content(content)
                .build();
    }

    public static PostComment createReply(Post post, User user, PostComment parent, String content) {
        Objects.requireNonNull(post, "게시글은 필수입니다.");
        Objects.requireNonNull(user, "사용자는 필수입니다.");
        Objects.requireNonNull(content, "댓글 내용은 필수입니다.");
        if (parent == null) {
            throw new PostDomainException("부모 댓글이 없습니다.");
        }
        if (!parent.getPost().getId().equals(post.getId())) {
            throw new PostDomainException("부모 댓글이 해당 게시글에 속하지 않습니다.");
        }
        if (parent.getDepth() >= 1) {
            throw new PostDomainException("대댓글은 1단계까지만 허용됩니다.");
        }
        return PostComment.builder()
                .post(post)
                .user(user)
                .parentComment(parent)
                .depth(parent.getDepth() + 1)
                .content(content)
                .build();
    }
}
