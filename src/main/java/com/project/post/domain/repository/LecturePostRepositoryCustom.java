package com.project.post.domain.repository;

import com.project.post.domain.repository.dto.LecturePostDetailQueryResult;
import com.project.post.domain.repository.dto.LecturePostListQueryResult;
import com.project.post.domain.repository.dto.LecturePostSearchCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;

import java.util.Optional;

public interface LecturePostRepositoryCustom {

    Page<LecturePostListQueryResult> findLecturePostList(
            @NonNull Pageable pageable,
            @NonNull LecturePostSearchCondition condition);

    Optional<LecturePostDetailQueryResult> findLecturePostDetail(@NonNull Long postId);
}
