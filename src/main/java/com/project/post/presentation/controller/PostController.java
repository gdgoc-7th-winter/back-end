package com.project.post.presentation.controller;

import com.project.post.presentation.swagger.PostControllerDocs;
import com.project.post.application.dto.LikeScrapToggleResponse;
import com.project.post.application.dto.PostCreateRequest;
import com.project.post.application.dto.PostDetailResponse;
import com.project.post.application.dto.PostListResponse;
import com.project.post.application.dto.PostUpdateRequest;
import com.project.global.annotation.CurrentUser;
import com.project.post.application.service.PostCommandService;
import com.project.post.application.service.PostLikeService;
import com.project.post.application.service.PostQueryService;
import com.project.post.application.service.PostScrapService;
import com.project.user.domain.entity.User;
import com.project.global.response.CommonResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@Validated
@RequiredArgsConstructor
public class PostController implements PostControllerDocs {

    private final PostCommandService postCommandService;
    private final PostQueryService postQueryService;
    private final PostLikeService postLikeService;
    private final PostScrapService postScrapService;

    @Override
    @GetMapping("/boards/{code}/posts")
    public ResponseEntity<CommonResponse<Page<PostListResponse>>> getList(
            @PathVariable @NotBlank @NonNull String code,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) @NonNull Pageable pageable) {
        Page<PostListResponse> list = postQueryService.getList(code, pageable);
        return ResponseEntity.ok(CommonResponse.ok(list));
    }

    @Override
    @GetMapping("/posts/{id}")
    public ResponseEntity<CommonResponse<PostDetailResponse>> getDetail(@PathVariable @Positive @NonNull Long id) {
        PostDetailResponse detail = postQueryService.getDetail(id);
        return ResponseEntity.ok(CommonResponse.ok(detail));
    }

    @Override
    @PostMapping("/posts/{id}/view")
    public ResponseEntity<CommonResponse<Void>> increaseViewCount(@PathVariable @Positive @NonNull Long id) {
        postCommandService.increaseViewCount(id);
        return ResponseEntity.ok(CommonResponse.ok());
    }

    @Override
    @PostMapping("/boards/{code}/posts")
    public ResponseEntity<CommonResponse<Long>> create(
            @PathVariable @NotBlank @NonNull String code,
            @RequestBody @Valid @NonNull PostCreateRequest request,
            @CurrentUser @NonNull User user) {
        Long postId = postCommandService.create(
                code,
                request,
                user);
        return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponse.ok(postId));
    }

    @Override
    @PatchMapping("/posts/{id}")
    public ResponseEntity<CommonResponse<Void>> update(
            @PathVariable @Positive @NonNull Long id,
            @RequestBody @Valid @NonNull PostUpdateRequest request,
            @CurrentUser @NonNull User user) {
        postCommandService.update(
                id,
                request,
                user);
        return ResponseEntity.ok(CommonResponse.ok());
    }

    @Override
    @DeleteMapping("/posts/{id}")
    public ResponseEntity<CommonResponse<Void>> delete(
            @PathVariable @Positive @NonNull Long id,
            @CurrentUser @NonNull User user) {
        postCommandService.softDelete(id, user);
        return ResponseEntity.ok(CommonResponse.ok());
    }

    @Override
    @PutMapping("/posts/{id}/like")
    public ResponseEntity<CommonResponse<LikeScrapToggleResponse>> like(
            @PathVariable @Positive @NonNull Long id,
            @CurrentUser @NonNull User user) {
        LikeScrapToggleResponse response = postLikeService.like(id, user);
        return ResponseEntity.ok(CommonResponse.ok(response));
    }

    @Override
    @DeleteMapping("/posts/{id}/like")
    public ResponseEntity<CommonResponse<LikeScrapToggleResponse>> unlike(
            @PathVariable @Positive @NonNull Long id,
            @CurrentUser @NonNull User user) {
        LikeScrapToggleResponse response = postLikeService.unlike(id, user);
        return ResponseEntity.ok(CommonResponse.ok(response));
    }

    @Override
    @PutMapping("/posts/{id}/scrap")
    public ResponseEntity<CommonResponse<LikeScrapToggleResponse>> scrap(
            @PathVariable @Positive @NonNull Long id,
            @CurrentUser @NonNull User user) {
        LikeScrapToggleResponse response = postScrapService.scrap(id, user);
        return ResponseEntity.ok(CommonResponse.ok(response));
    }

    @Override
    @DeleteMapping("/posts/{id}/scrap")
    public ResponseEntity<CommonResponse<LikeScrapToggleResponse>> unscrap(
            @PathVariable @Positive @NonNull Long id,
            @CurrentUser @NonNull User user) {
        LikeScrapToggleResponse response = postScrapService.unscrap(id, user);
        return ResponseEntity.ok(CommonResponse.ok(response));
    }
}
