package com.project.user.application.dto.response;

import com.project.user.domain.entity.Department;

public record DepartmentResponse(
        Long id,
        String college,
        String name
) {
    public static DepartmentResponse from(Department department) {
        return new DepartmentResponse(
                department.getId(),
                department.getCollege(),
                department.getName()
        );
    }
}
