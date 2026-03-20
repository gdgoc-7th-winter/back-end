package com.project.user.config;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DepartmentsDto {

    private List<DepartmentEntry> departments;

    @Getter
    @Setter
    public static class DepartmentEntry {
        private String college;
        private String name;
    }
}
