package com.project.post.domain.repository;

import com.project.post.domain.entity.PostAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostAttachmentRepository extends JpaRepository<PostAttachment, Long> {

    @Query("SELECT pa FROM PostAttachment pa WHERE pa.post.id = :postId ORDER BY pa.sortOrder ASC")
    List<PostAttachment> findByPostIdOrderBySortOrderAsc(@Param("postId") Long postId);

    @Modifying
    @Query("DELETE FROM PostAttachment pa WHERE pa.post.id = :postId")
    void deleteByPostId(@Param("postId") Long postId);
}
