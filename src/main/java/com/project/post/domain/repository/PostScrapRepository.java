package com.project.post.domain.repository;

import com.project.post.domain.entity.PostScrap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PostScrapRepository extends JpaRepository<PostScrap, Long> {

    @Override
    @NonNull
    <S extends PostScrap> S save(@NonNull S entity);

    @Query("SELECT ps FROM PostScrap ps WHERE ps.post.id = :postId AND ps.user.id = :userId")
    Optional<PostScrap> findByPostIdAndUserId(@Param("postId") Long postId, @Param("userId") Long userId);

    @Query("SELECT (COUNT(ps) > 0) FROM PostScrap ps WHERE ps.post.id = :postId AND ps.user.id = :userId")
    boolean existsByPostIdAndUserId(@Param("postId") Long postId, @Param("userId") Long userId);

    @Query("SELECT ps.post.id FROM PostScrap ps WHERE ps.post.id IN :postIds AND ps.user.id = :userId")
    List<Long> findPostIdsScrappedByUserAndPostIdIn(@Param("postIds") Collection<Long> postIds, @Param("userId") Long userId);

    @Modifying
    @Query(value = "INSERT INTO post_scraps (post_id, user_id, created_at) " +
            "VALUES (:postId, :userId, NOW()) " +
            "ON CONFLICT ON CONSTRAINT uk_post_scraps_post_user DO NOTHING",
            nativeQuery = true)
    int insertIfAbsent(@Param("postId") Long postId, @Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM PostScrap ps WHERE ps.post.id = :postId AND ps.user.id = :userId")
    int deleteByPostIdAndUserId(@Param("postId") Long postId, @Param("userId") Long userId);
}
