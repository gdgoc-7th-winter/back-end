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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PostController implements PostControllerDocs {

    private final PostCommandService postCommandService;
    private final PostQueryService postQueryService;
    private final PostLikeService postLikeService;
    private final PostScrapService postScrapService;

    @Override
    @GetMapping("/boards/{code}/posts")
    public ResponseEntity<CommonResponse<Page<PostListResponse>>> getList(
            @PathVariable String code,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<PostListResponse> list = postQueryService.getList(Objects.requireNonNull(code), Objects.requireNonNull(pageable));
        return ResponseEntity.ok(CommonResponse.ok(list));
    }

    @Override
    @GetMapping("/posts/{id}")
    public ResponseEntity<CommonResponse<PostDetailResponse>> getDetail(@PathVariable Long id) {
        Long postId = Objects.requireNonNull(id);
        postCommandService.increaseViewCount(postId);
        PostDetailResponse detail = postQueryService.getDetail(postId);
        return ResponseEntity.ok(CommonResponse.ok(detail));
    }

    @Override
    @PostMapping("/boards/{code}/posts")
    public ResponseEntity<CommonResponse<Long>> create(
            @PathVariable String code,
            @RequestBody @Valid PostCreateRequest request,
            @CurrentUser User user) {
        Long postId = postCommandService.create(
                Objects.requireNonNull(code),
                Objects.requireNonNull(request),
                Objects.requireNonNull(user));
        return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponse.ok(postId));
    }

    @Override
    @PatchMapping("/posts/{id}")
    public ResponseEntity<CommonResponse<Void>> update(
            @PathVariable Long id,
            @RequestBody @Valid PostUpdateRequest request,
            @CurrentUser User user) {
        postCommandService.update(
                Objects.requireNonNull(id),
                Objects.requireNonNull(request),
                Objects.requireNonNull(user));
        return ResponseEntity.ok(CommonResponse.ok());
    }

    @Override
    @DeleteMapping("/posts/{id}")
    public ResponseEntity<CommonResponse<Void>> delete(
            @PathVariable Long id,
            @CurrentUser User user) {
        postCommandService.softDelete(Objects.requireNonNull(id), Objects.requireNonNull(user));
        return ResponseEntity.ok(CommonResponse.ok());
    }

    @Override
    @PostMapping("/posts/{id}/like")
    public ResponseEntity<CommonResponse<LikeScrapToggleResponse>> toggleLike(
            @PathVariable Long id,
            @CurrentUser User user) {
        LikeScrapToggleResponse response = postLikeService.toggle(Objects.requireNonNull(id), Objects.requireNonNull(user));
        return ResponseEntity.ok(CommonResponse.ok(response));
    }

    @Override
    @PostMapping("/posts/{id}/scrap")
    public ResponseEntity<CommonResponse<LikeScrapToggleResponse>> toggleScrap(
            @PathVariable Long id,
            @CurrentUser User user) {
        LikeScrapToggleResponse response = postScrapService.toggle(Objects.requireNonNull(id), Objects.requireNonNull(user));
        return ResponseEntity.ok(CommonResponse.ok(response));
    }
}
