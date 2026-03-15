package com.project.post.domain.entity;

import com.project.global.entity.SoftDeleteEntity;
import com.project.post.domain.enums.Campus;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "lecture_posts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
@AttributeOverride(name = "id", column = @Column(name = "post_id"))
@SQLRestriction("deleted_at IS NULL")
public class LecturePost extends SoftDeleteEntity {

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(name = "department", nullable = false, length = 100)
    private String department;

    @Column(name = "campus", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private Campus campus;

    public void update(String department, Campus campus) {
        if (department != null && !department.isBlank()) {
            this.department = department;
        }
        if (campus != null) {
            this.campus = campus;
        }
    }
}
