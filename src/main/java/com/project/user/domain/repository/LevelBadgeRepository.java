package com.project.user.domain.repository;

import com.project.user.domain.entity.LevelBadge;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LevelBadgeRepository extends JpaRepository<LevelBadge, Long> {
    Optional<LevelBadge> getLevelBadgeById(Long id);

    @Query("SELECT lb FROM LevelBadge lb WHERE :point BETWEEN lb.minimumPoint AND lb.maximumPoint")
    Optional<LevelBadge> findByPointWithinRange(@Param("point") Integer point);
}
