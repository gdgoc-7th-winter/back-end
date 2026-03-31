package com.project.post.domain.repository;

import com.project.post.domain.entity.ApplicationSubmission;
import com.project.post.domain.entity.RecruitingApplication;
import com.project.post.domain.repository.dto.AppliedRecruitingPostListQueryResult;
import com.project.user.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ApplicationSubmissionRepository
        extends JpaRepository<ApplicationSubmission, Long>,
        JpaSpecificationExecutor<ApplicationSubmission> {

    boolean existsByRecruitingApplicationAndUserAndDeletedAtIsNull(
            RecruitingApplication recruitingApplication,
            User user
    );

    boolean existsByRecruitingApplicationAndDeletedAtIsNull(
            RecruitingApplication recruitingApplication
    );

    List<ApplicationSubmission> findAllByRecruitingApplicationAndDeletedAtIsNullOrderBySubmittedAtDesc(
            RecruitingApplication recruitingApplication
    );

    List<ApplicationSubmission> findAllByUserAndDeletedAtIsNullOrderBySubmittedAtDesc(User user);

    Optional<ApplicationSubmission> findByIdAndDeletedAtIsNull(Long id);

    @Query("""
        select new com.project.post.domain.repository.dto.AppliedRecruitingPostListQueryResult(
            s.id,
            rp.category,
            rp.startedAt,
            rp.deadlineAt,
            s.submittedAt,
            p.id,
            p.title,
            p.thumbnailUrl,
            author.id,
            author.nickname,
            author.profileImgUrl,
            dept.name,
            null,
            lb.levelImage,
            case when author.deletedAt is null then false else true end,
            p.viewCount,
            p.likeCount,
            p.scrapCount,
            p.commentCount,
            p.createdAt
        )
        from ApplicationSubmission s
        join s.recruitingApplication ra
        join ra.recruitingPost rp
        join rp.post p
        join p.author author
        left join author.department dept
        left join author.levelBadge lb
        where s.user.id = :userId
          and s.deletedAt is null
          and p.deletedAt is null
        order by s.submittedAt desc
    """)
    List<AppliedRecruitingPostListQueryResult> findAppliedRecruitingPostListByUserId(
            @Param("userId") Long userId
    );
}