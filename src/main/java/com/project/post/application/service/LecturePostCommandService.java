package com.project.post.application.service;

import com.project.post.application.dto.LecturePost.LecturePostCreateRequest;
import com.project.post.application.dto.LecturePost.LecturePostUpdateRequest;
import com.project.user.domain.entity.User;
import org.springframework.lang.NonNull;

public interface LecturePostCommandService {

    Long create(@NonNull LecturePostCreateRequest request, @NonNull User author);

    void update(@NonNull Long postId, @NonNull LecturePostUpdateRequest request, @NonNull User author);

    void delete(@NonNull Long postId, @NonNull User author);
}
