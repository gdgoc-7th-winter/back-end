package com.project.contribution.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name="contributionScore")
@Getter
@NoArgsConstructor
public class ContributionScore {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="cont_id")
    private Long id;

    @Column(name="cont_name", unique=true, nullable=false)
    private String name;

    @Column(name="cont_point", nullable=false)
    private Integer point;

    @Builder
    public ContributionScore(String name, Integer point) {
        this.name = name;
        this.point = point;
    }

    public void update(String name, Integer point) {
        if (name != null) {
            this.name = name;
        }
        if (point != null) {
            this.point = point;
        }
    }
}
