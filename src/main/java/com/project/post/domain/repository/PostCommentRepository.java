package com.project.post.domain.repository;

import com.project.post.domain.entity.PostComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PostCommentRepository extends JpaRepository<PostComment, Long>, PostCommentRepositoryCustom {

    @Override
    @NonNull
    <S extends PostComment> S save(@NonNull S entity);

    @Query("SELECT c FROM PostComment c WHERE c.id = :commentId AND c.deletedAt IS NULL")
    Optional<PostComment> findActiveById(@Param("commentId") Long commentId);

    @Query("SELECT (COUNT(c) > 0) FROM PostComment c WHERE c.id = :commentId AND c.deletedAt IS NULL")
    boolean existsActiveById(@Param("commentId") Long commentId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE PostComment c SET c.likeCount = c.likeCount + 1 WHERE c.id = :commentId AND c.deletedAt IS NULL")
    int incrementLikeCount(@Param("commentId") Long commentId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE PostComment c SET c.likeCount = CASE WHEN c.likeCount > 0 THEN c.likeCount - 1 ELSE 0 END WHERE c.id = :commentId AND c.deletedAt IS NULL")
    int decrementLikeCount(@Param("commentId") Long commentId);

    @Query("SELECT c.likeCount FROM PostComment c WHERE c.id = :commentId AND c.deletedAt IS NULL")
    Optional<Long> findLikeCountById(@Param("commentId") Long commentId);

    @Query("SELECT c FROM PostComment c "
            + "JOIN FETCH c.user JOIN FETCH c.post "
            + "WHERE c.id IN :ids")
    List<PostComment> findAllByIdInWithAssociations(@Param("ids") Collection<Long> ids);
}
