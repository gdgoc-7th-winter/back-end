package com.project.user.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import jakarta.persistence.Id;

@Entity
@Table(name="levelBadge")
@Getter
@Setter
@NoArgsConstructor
public class LevelBadge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;

    @Column(name="levelName")
    private String levelName;

    @Column(name="levelDescription")
    private String levelDescription;

    @Column(name="levelImage")
    private String levelImage;

    @Column(name="minimumPoint")
    private Integer minimumPoint;

    @Column(name="maximumPoint")
    private Integer maximumPoint;

    @Builder
    public LevelBadge (String levelName, String levelDescription, String levelImage, Integer minimumPoint, Integer maximumPoint) {
        this.levelName = levelName;
        this.levelDescription = levelDescription;
        this.levelImage = levelImage;
        this.minimumPoint = minimumPoint;
        this.maximumPoint = maximumPoint;
    }

    public void update(String name, String description, String image, Integer minimumPoint, Integer maximumPoint) {
        if (name != null) {
            this.levelName = name;
        }
        if (description != null) {
            this.levelDescription = description;
        }
        if (image != null) {
            this.levelImage = image;
        }
        if (minimumPoint != null) {
            this.minimumPoint = minimumPoint;
        }
        if (maximumPoint != null) {
            this.maximumPoint = maximumPoint;
        }
    }
}
