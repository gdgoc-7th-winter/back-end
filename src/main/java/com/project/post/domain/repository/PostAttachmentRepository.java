package com.project.post.domain.repository;

import com.project.post.domain.entity.PostAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostAttachmentRepository extends JpaRepository<PostAttachment, Long> {

    @Modifying(flushAutomatically = true)
    @Query("DELETE FROM PostAttachment pa WHERE pa.post.id = :postId")
    void deleteByPostId(@Param("postId") Long postId);
}
