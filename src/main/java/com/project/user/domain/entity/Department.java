package com.project.user.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "departments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "department_id")
    private Long id;

    @Column(name = "college", nullable = false, length = 100)
    private String college;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Builder
    public Department(String college, String name) {
        this.college = college;
        this.name = name;
    }
}
