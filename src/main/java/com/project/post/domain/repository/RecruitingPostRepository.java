package com.project.post.domain.repository;

import com.project.post.domain.entity.RecruitingPost;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RecruitingPostRepository extends JpaRepository<RecruitingPost, Long>, RecruitingPostRepositoryCustom {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select rp from RecruitingPost rp where rp.id = :postId")
    Optional<RecruitingPost> findByIdForUpdate(@Param("postId") Long postId);

    Optional<RecruitingPost> findByIdAndDeletedAtIsNull(Long id);

    List<RecruitingPost> findAllByPostAuthorIdAndDeletedAtIsNullAndPostDeletedAtIsNullOrderByCreatedAtDesc(Long authorId);
}