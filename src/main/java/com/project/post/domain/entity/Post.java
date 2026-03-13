package com.project.post.domain.entity;

import com.project.global.entity.SoftDeleteEntity;
import com.project.post.domain.exception.PostDomainException;
import com.project.user.domain.entity.User;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    private long viewCount = 0;

    @Column(name = "like_count", nullable = false)
    @lombok.Builder.Default
    private long likeCount = 0;

    @Column(name = "scrap_count", nullable = false)
    @lombok.Builder.Default
    private long scrapCount = 0;

    @Column(name = "comment_count", nullable = false)
    @lombok.Builder.Default
    private long commentCount = 0;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 100)
    @lombok.Builder.Default
    private List<PostTag> postTags = new ArrayList<>();

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

    public void replaceTags(List<Tag> tags) {
        if (tags == null) {
            return;
        }

        // 입력 태그를 식별자 기준으로 중복 제거
        Map<String, Tag> incomingByTagIdentity = new LinkedHashMap<>();
        for (Tag tag : tags) {
            String identity = tagIdentity(tag);
            if (identity == null) {
                continue;
            }
            incomingByTagIdentity.putIfAbsent(identity, tag);
        }

        Set<String> incomingTagIds = new LinkedHashSet<>(incomingByTagIdentity.keySet());
        Iterator<PostTag> iterator = this.postTags.iterator();
        while (iterator.hasNext()) {
            PostTag postTag = iterator.next();
            String existingTagId = tagIdentity(postTag.getTag());
            if (!incomingTagIds.contains(existingTagId)) {
                iterator.remove();
            }
        }

        Set<String> existingTagIds = new LinkedHashSet<>();
        for (PostTag postTag : this.postTags) {
            existingTagIds.add(tagIdentity(postTag.getTag()));
        }

        for (Map.Entry<String, Tag> entry : incomingByTagIdentity.entrySet()) {
            if (existingTagIds.contains(entry.getKey())) {
                continue;
            }
            addPostTag(entry.getValue());
        }
    }

    private String tagIdentity(Tag tag) {
        if (tag == null) {
            return null;
        }
        if (tag.getId() != null) {
            return "ID:" + tag.getId();
        }
        if (tag.getName() == null) {
            return null;
        }
        return "NAME:" + tag.getName();
    }

    private void addPostTag(Tag tag) {
        PostTag postTag = new PostTag(this, tag);
        this.postTags.add(postTag);
    }

    private void validateNotBlank(String value, String message) {
        if (value.isBlank()) {
            throw new PostDomainException(message);
        }
    }
}
