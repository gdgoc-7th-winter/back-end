package com.project.user.application.service.impl;

import com.project.user.application.dto.response.DepartmentResponse;
import com.project.user.application.service.DepartmentService;
import com.project.user.domain.repository.DepartmentRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentResponse> getDepartments(String college, String name) {
        if (college != null) {
            return departmentRepository.findByCollege(college).stream()
                    .map(DepartmentResponse::from)
                    .toList();
        }
        if (name != null) {
            return departmentRepository.findByNameContaining(name).stream()
                    .map(DepartmentResponse::from)
                    .toList();
        }
        return departmentRepository.findAll().stream()
                .map(DepartmentResponse::from)
                .toList();
    }
}
