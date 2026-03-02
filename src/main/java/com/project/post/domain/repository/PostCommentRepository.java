package com.project.post.domain.repository;

import com.project.post.domain.entity.PostComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;

import java.util.Optional;

public interface PostCommentRepository extends JpaRepository<PostComment, Long>, PostCommentRepositoryCustom {

    @Override
    @NonNull
    <S extends PostComment> S save(@NonNull S entity);

    @Query("SELECT c FROM PostComment c WHERE c.id = :commentId AND c.deletedAt IS NULL")
    Optional<PostComment> findActiveById(@Param("commentId") Long commentId);
}
