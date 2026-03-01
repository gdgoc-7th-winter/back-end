package com.project.post.domain.repository;

import com.project.post.domain.entity.PostTag;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostTagRepository extends JpaRepository<PostTag, PostTag.PostTagId> {

    @EntityGraph(attributePaths = {"tag"})
    @Query("SELECT pt FROM PostTag pt WHERE pt.post.id = :postId")
    List<PostTag> findByPostId(@Param("postId") Long postId);

    @Modifying
    @Query("DELETE FROM PostTag pt WHERE pt.post.id = :postId")
    void deleteByPostId(@Param("postId") Long postId);
}
