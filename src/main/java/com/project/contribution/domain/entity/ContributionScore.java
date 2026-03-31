package com.project.contribution.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "contribution_score")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ContributionScore {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cont_id")
    private Long id;

    @Column(name = "cont_code", unique = true, nullable = false, length = 64)
    private String code;

    @Column(name = "cont_name", unique = true, nullable = false)
    private String name;

    @Column(name = "cont_point", nullable = false)
    private Integer point;

    public void update(String name, Integer point) {
        if (name != null) {
            this.name = name;
        }
        if (point != null) {
            this.point = point;
        }
    }
}
