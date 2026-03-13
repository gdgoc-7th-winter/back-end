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
import lombok.Setter;

@Entity
@Table(name="contributionBadge")
@Getter
@Setter
@NoArgsConstructor
public class ContributionBadge {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="cont_id")
    private Long id;

    @Column(name="cont_name")
    private String name;

    @Column(name="cont_description")
    private String badgeDescription;

    @Column(name="cont_image")
    private String badgeImage;

    @Column(name="cont_point")
    private Integer point;

    @Builder
    public ContributionBadge(String name, String badgeDescription, String badgeImage, Integer point) {
        this.name = name;
        this.badgeDescription = badgeDescription;
        this.badgeImage = badgeImage;
        this.point = point;
    }

    public void update(String name, String description, String image, Integer point) {
        if (name != null) {
            this.name = name;
        }
        if (description != null) {
            this.badgeDescription = description;
        }
        if (image != null) {
            this.badgeImage = image;
        }
        if (point != null) {
            this.point = point;
        }
    }
}
