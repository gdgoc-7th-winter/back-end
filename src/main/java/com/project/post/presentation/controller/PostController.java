package com.project.post.presentation.controller;

import com.project.global.annotation.CurrentUser;
import com.project.global.annotation.OptionalSessionUser;
import com.project.global.response.CommonResponse;
import com.project.global.response.PageResponse;
import com.project.post.application.dto.LikeScrapToggleResponse;
import com.project.post.application.dto.PostCreateRequest;
import com.project.post.application.dto.PostCreateResponse;
import com.project.post.application.dto.PostDetailResponse;
import com.project.post.application.dto.PostListResponse;
import com.project.post.application.dto.PostUpdateRequest;
import com.project.post.application.service.PostCommandService;
import com.project.post.application.service.PostLikeService;
import com.project.post.application.service.PostQueryService;
import com.project.post.application.service.PostScrapService;
import com.project.post.presentation.support.ViewerUserId;
import com.project.post.presentation.swagger.PostControllerDocs;
import com.project.user.domain.entity.User;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

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
    public ResponseEntity<CommonResponse<PageResponse<PostListResponse>>> getList(
            @PathVariable @NotBlank @NonNull String code,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, name = "tags") List<String> tags,
            @RequestParam(required = false, defaultValue = "latest") String order,
            @ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) @NonNull Pageable pageable,
            @OptionalSessionUser Optional<User> optionalViewer) {
        Page<PostListResponse> list = postQueryService.getList(
                code, pageable, keyword, tags, order, ViewerUserId.from(optionalViewer));
        return ResponseEntity.ok(CommonResponse.ok(PageResponse.of(list)));
    }

    @Override
    @GetMapping("/posts/{postId}")
    public ResponseEntity<CommonResponse<PostDetailResponse>> getDetail(
            @PathVariable @Positive @NonNull Long postId,
            @OptionalSessionUser Optional<User> optionalViewer) {
        PostDetailResponse detail = postQueryService.getDetail(postId, ViewerUserId.from(optionalViewer));
        return ResponseEntity.ok(CommonResponse.ok(detail));
    }

    @Override
    @PostMapping("/posts/{postId}/view")
    public ResponseEntity<CommonResponse<Void>> increaseViewCount(@PathVariable @Positive @NonNull Long postId) {
        postCommandService.increaseViewCount(postId);
        return ResponseEntity.ok(CommonResponse.ok());
    }

    @Override
    @PostMapping("/boards/{code}/posts")
    public ResponseEntity<CommonResponse<PostCreateResponse>> create(
            @PathVariable @NotBlank @NonNull String code,
            @RequestBody @Valid @NonNull PostCreateRequest request,
            @CurrentUser @NonNull User user) {
        var post = postCommandService.create(code, request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponse.ok(new PostCreateResponse(post.getId())));
    }

    @Override
    @PatchMapping("/posts/{postId}")
    public ResponseEntity<CommonResponse<Void>> update(
            @PathVariable @Positive @NonNull Long postId,
            @RequestBody @Valid @NonNull PostUpdateRequest request,
            @CurrentUser @NonNull User user) {
        postCommandService.update(
                postId,
                request,
                user);
        return ResponseEntity.ok(CommonResponse.ok());
    }

    @Override
    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<CommonResponse<Void>> delete(
            @PathVariable @Positive @NonNull Long postId,
            @CurrentUser @NonNull User user) {
        postCommandService.softDelete(postId, user);
        return ResponseEntity.ok(CommonResponse.ok());
    }

    @Override
    @PutMapping("/posts/{postId}/like")
    public ResponseEntity<CommonResponse<LikeScrapToggleResponse>> like(
            @PathVariable @Positive @NonNull Long postId,
            @CurrentUser @NonNull User user) {
        LikeScrapToggleResponse response = postLikeService.like(postId, user);
        return ResponseEntity.ok(CommonResponse.ok(response));
    }

    @Override
    @DeleteMapping("/posts/{postId}/like")
    public ResponseEntity<CommonResponse<LikeScrapToggleResponse>> unlike(
            @PathVariable @Positive @NonNull Long postId,
            @CurrentUser @NonNull User user) {
        LikeScrapToggleResponse response = postLikeService.unlike(postId, user);
        return ResponseEntity.ok(CommonResponse.ok(response));
    }

    @Override
    @PutMapping("/posts/{postId}/scrap")
    public ResponseEntity<CommonResponse<LikeScrapToggleResponse>> scrap(
            @PathVariable @Positive @NonNull Long postId,
            @CurrentUser @NonNull User user) {
        LikeScrapToggleResponse response = postScrapService.scrap(postId, user);
        return ResponseEntity.ok(CommonResponse.ok(response));
    }

    @Override
    @DeleteMapping("/posts/{postId}/scrap")
    public ResponseEntity<CommonResponse<LikeScrapToggleResponse>> unscrap(
            @PathVariable @Positive @NonNull Long postId,
            @CurrentUser @NonNull User user) {
        LikeScrapToggleResponse response = postScrapService.unscrap(postId, user);
        return ResponseEntity.ok(CommonResponse.ok(response));
    }
}
