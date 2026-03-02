package com.project.post.domain.repository;

import com.project.post.domain.entity.PostScrap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;

import java.util.Optional;

public interface PostScrapRepository extends JpaRepository<PostScrap, Long> {

    @Override
    @NonNull
    <S extends PostScrap> S save(@NonNull S entity);

    @Query("SELECT ps FROM PostScrap ps WHERE ps.post.id = :postId AND ps.user.id = :userId")
    Optional<PostScrap> findByPostIdAndUserId(@Param("postId") Long postId, @Param("userId") Long userId);

    @Query("SELECT (COUNT(ps) > 0) FROM PostScrap ps WHERE ps.post.id = :postId AND ps.user.id = :userId")
    boolean existsByPostIdAndUserId(@Param("postId") Long postId, @Param("userId") Long userId);
}
