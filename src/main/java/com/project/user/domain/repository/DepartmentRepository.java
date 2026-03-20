package com.project.user.domain.repository;

import com.project.user.domain.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    List<Department> findByCollege(String college);

    List<Department> findByNameContaining(String name);
}
