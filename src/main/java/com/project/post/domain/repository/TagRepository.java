package com.project.post.domain.repository;

import com.project.post.domain.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {

    Optional<Tag> findByName(String name);

    List<Tag> findByNameIn(List<String> names);

    @Modifying
    @Query(value = "INSERT INTO tags (name) VALUES (:name) ON CONFLICT (name) DO NOTHING",
            nativeQuery = true)
    int insertIfAbsent(@Param("name") String name);
}
