package com.project.post.domain.repository;

import com.project.post.domain.entity.RecruitingApplication;
import com.project.post.domain.entity.RecruitingPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface RecruitingApplicationRepository extends JpaRepository<RecruitingApplication, Long> {

    Optional<RecruitingApplication> findByRecruitingPost(RecruitingPost recruitingPost);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select ra
        from RecruitingApplication ra
        where ra.recruitingPost = :recruitingPost
    """)
    Optional<RecruitingApplication> findByRecruitingPostForUpdate(
            @Param("recruitingPost") RecruitingPost recruitingPost
    );
}
