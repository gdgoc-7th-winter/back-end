package com.project.post.presentation.controller;

import com.project.global.annotation.CurrentUser;
import com.project.global.response.CommonResponse;
import com.project.global.response.PageResponse;
import com.project.post.application.dto.LecturePost.LecturePostCreateRequest;
import com.project.post.application.dto.LecturePost.LecturePostDetailResponse;
import com.project.post.application.dto.LecturePost.LecturePostListResponse;
import com.project.post.application.dto.LecturePost.LecturePostUpdateRequest;
import com.project.post.application.dto.PostCreateResponse;
import com.project.post.application.service.LecturePostCommandService;
import com.project.post.application.service.LecturePostQueryService;
import com.project.post.domain.enums.Campus;
import com.project.post.presentation.swagger.LecturePostControllerDocs;
import com.project.user.domain.entity.User;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/lectures")
@Validated
@RequiredArgsConstructor
public class LecturePostController implements LecturePostControllerDocs {

    private final LecturePostCommandService lecturePostCommandService;
    private final LecturePostQueryService lecturePostQueryService;

    @Override
    @GetMapping
    public ResponseEntity<CommonResponse<PageResponse<LecturePostListResponse>>> getList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, name = "tags") List<String> tags,
            @RequestParam(required = false) Campus campus,
            @RequestParam(required = false, name = "departments") List<String> departments,
            @RequestParam(required = false, defaultValue = "latest") String order,
            @ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) @NonNull Pageable pageable) {

        Page<LecturePostListResponse> list = lecturePostQueryService.getList(
                pageable, keyword, tags, campus, departments, order);
        return ResponseEntity.ok(CommonResponse.ok(PageResponse.of(list)));
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<LecturePostDetailResponse>> getDetail(
            @PathVariable @Positive @NonNull Long id) {
        LecturePostDetailResponse detail = lecturePostQueryService.getDetail(id);
        return ResponseEntity.ok(CommonResponse.ok(detail));
    }

    @Override
    @PostMapping
    public ResponseEntity<CommonResponse<PostCreateResponse>> create(
            @RequestBody @Valid @NonNull LecturePostCreateRequest request,
            @CurrentUser @NonNull User user) {
        Long postId = lecturePostCommandService.create(request, user);
        PostCreateResponse response = new PostCreateResponse(postId);
        return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponse.ok(response));
    }

    @Override
    @PatchMapping("/{id}")
    public ResponseEntity<CommonResponse<Void>> update(
            @PathVariable @Positive @NonNull Long id,
            @RequestBody @Valid @NonNull LecturePostUpdateRequest request,
            @CurrentUser @NonNull User user) {
        lecturePostCommandService.update(id, request, user);
        return ResponseEntity.ok(CommonResponse.ok());
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponse<Void>> delete(
            @PathVariable @Positive @NonNull Long id,
            @CurrentUser @NonNull User user) {
        lecturePostCommandService.delete(id, user);
        return ResponseEntity.ok(CommonResponse.ok());
    }
}
