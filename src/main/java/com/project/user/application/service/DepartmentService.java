package com.project.user.application.service;

import com.project.user.application.dto.response.DepartmentResponse;

import java.util.List;

public interface DepartmentService {

    List<DepartmentResponse> getDepartments(String college, String name);
}
