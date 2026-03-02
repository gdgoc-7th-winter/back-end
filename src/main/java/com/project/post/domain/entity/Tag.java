package com.project.post.domain.entity;

import com.project.post.domain.exception.PostDomainException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tags")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Tag {

    private static final int MAX_NAME_LENGTH = 50;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tag_id")
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = MAX_NAME_LENGTH)
    private String name;

    public Tag(String name) {
        if (name == null || name.isBlank()) {
            throw new PostDomainException("태그 이름은 필수입니다.");
        }
        String trimmed = name.trim();
        if (trimmed.length() > MAX_NAME_LENGTH) {
            throw new PostDomainException("태그 이름은 " + MAX_NAME_LENGTH + "자를 초과할 수 없습니다.");
        }
        this.name = trimmed;
    }
}
