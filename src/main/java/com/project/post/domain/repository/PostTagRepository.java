package com.project.post.domain.repository;

import com.project.post.domain.entity.PostTag;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostTagRepository extends JpaRepository<PostTag, Long> {

    @EntityGraph(attributePaths = {"tag", "post"})
    @Query("SELECT pt FROM PostTag pt WHERE pt.post.id = :postId")
    List<PostTag> findByPostId(@Param("postId") Long postId);

    @EntityGraph(attributePaths = {"tag", "post"})
    @Query("SELECT pt FROM PostTag pt WHERE pt.post.id IN :postIds")
    List<PostTag> findByPostIdIn(@Param("postIds") List<Long> postIds);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM PostTag pt WHERE pt.post.id = :postId")
    void deleteByPostId(@Param("postId") Long postId);
}
