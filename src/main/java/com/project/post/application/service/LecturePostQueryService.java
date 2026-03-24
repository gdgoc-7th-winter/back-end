package com.project.post.application.service;

import com.project.post.application.dto.LecturePost.LecturePostDetailResponse;
import com.project.post.application.dto.LecturePost.LecturePostListResponse;
import com.project.post.domain.enums.Campus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.List;

public interface LecturePostQueryService {

    Page<LecturePostListResponse> getList(
            @NonNull Pageable pageable,
            String keyword,
            List<String> tagNames,
            Campus campus,
            List<String> departments,
            String order,
            @Nullable Long viewerUserId);

    LecturePostDetailResponse getDetail(@NonNull Long postId, @Nullable Long viewerUserId);
}
