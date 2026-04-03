package com.project.post.domain.repository;

import com.project.post.domain.entity.RecruitingPost;
import com.project.post.domain.repository.dto.MyRecruitingPostQueryResult;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RecruitingPostRepository extends JpaRepository<RecruitingPost, Long>, RecruitingPostRepositoryCustom {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select rp from RecruitingPost rp where rp.id = :postId")
    Optional<RecruitingPost> findByIdForUpdate(@Param("postId") Long postId);

    Optional<RecruitingPost> findByIdAndDeletedAtIsNull(Long id);

    Page<RecruitingPost> findAllByPostAuthorIdAndDeletedAtIsNullAndPostDeletedAtIsNull(
            Long authorId,
            Pageable pageable
    );

    @Query("""
        select new com.project.post.domain.repository.dto.MyRecruitingPostQueryResult(
            rp.id,
            p.title,
            substring(p.content, 1, 100),
            p.thumbnailUrl,
            a.nickname,
            p.viewCount,
            p.likeCount,
            p.commentCount,
            p.createdAt,
            rp.category,
            rp.startedAt,
            rp.deadlineAt
        )
        from RecruitingPost rp
        join rp.post p
        join p.author a
        where a.id = :authorId
          and rp.deletedAt is null
          and p.deletedAt is null
    """)
    Page<MyRecruitingPostQueryResult> findMyRecruitingPostList(
            @Param("authorId") Long authorId,
            Pageable pageable
    );
}
