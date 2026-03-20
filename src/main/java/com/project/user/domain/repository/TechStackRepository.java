package com.project.user.domain.repository;

import com.project.user.domain.entity.TechStack;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TechStackRepository extends JpaRepository<TechStack, Long> {
    // 이름 리스트를 전달받아 해당하는 모든 TechStack 엔티티를 조회합니다.
    List<TechStack> findByNameIn(List<String> names);
}
