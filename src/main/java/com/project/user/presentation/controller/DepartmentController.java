package com.project.user.presentation.controller;

import com.project.global.response.CommonResponse;
import com.project.user.application.dto.response.DepartmentResponse;
import com.project.user.application.service.DepartmentService;
import com.project.user.presentation.swagger.DepartmentControllerDocs;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/departments")
@RequiredArgsConstructor
public class DepartmentController implements DepartmentControllerDocs {

    private final DepartmentService departmentService;

    @Override
    @GetMapping
    public ResponseEntity<CommonResponse<List<DepartmentResponse>>> getDepartments(
            @RequestParam(required = false) String college,
            @RequestParam(required = false) String name
    ) {
        return ResponseEntity.ok(CommonResponse.ok(departmentService.getDepartments(college, name)));
    }
}
