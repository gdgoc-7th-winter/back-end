package com.project.post.presentation.controller;

import com.project.post.presentation.swagger.PostCommentControllerDocs;
import com.project.post.application.dto.LikeScrapToggleResponse;
import com.project.post.application.dto.PostCommentCreateResponse;
import com.project.post.application.dto.PostCommentRequest;
import com.project.post.application.dto.PostCommentResponse;
import com.project.global.annotation.CurrentUser;
import com.project.post.application.service.PostCommentCommandService;
import com.project.post.application.service.PostCommentLikeService;
import com.project.post.application.service.PostCommentQueryService;
import com.project.user.domain.entity.User;
import com.project.global.response.CommonResponse;
import com.project.global.response.PageResponse;
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
public class PostCommentController implements PostCommentControllerDocs {

    private final PostCommentCommandService postCommentCommandService;
    private final PostCommentQueryService postCommentQueryService;
    private final PostCommentLikeService postCommentLikeService;

    @Override
    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<CommonResponse<PostCommentCreateResponse>> createComment(
            @PathVariable @Positive @NonNull Long postId,
            @RequestBody @Valid @NonNull PostCommentRequest request,
            @CurrentUser @NonNull User user) {
        Long commentId = postCommentCommandService.create(postId, request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponse.ok(new PostCommentCreateResponse(commentId)));
    }

    @Override
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<CommonResponse<PageResponse<PostCommentResponse>>> getComments(
            @PathVariable @Positive @NonNull Long postId,
            @ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.ASC) @NonNull Pageable pageable) {
        Page<PostCommentResponse> comments = postCommentQueryService.getComments(postId, pageable);
        return ResponseEntity.ok(CommonResponse.ok(PageResponse.of(comments)));
    }

    @Override
    @DeleteMapping("/posts/{postId}/comments/{commentId}")
    public ResponseEntity<CommonResponse<Void>> deleteComment(
            @PathVariable @Positive @NonNull Long postId,
            @PathVariable @Positive @NonNull Long commentId,
            @CurrentUser @NonNull User user) {
        postCommentCommandService.softDelete(
                postId,
                commentId,
                user);
        return ResponseEntity.ok(CommonResponse.ok());
    }

    @Override
    @PutMapping("/posts/{postId}/comments/{commentId}/like")
    public ResponseEntity<CommonResponse<LikeScrapToggleResponse>> likeComment(
            @PathVariable @Positive @NonNull Long postId,
            @PathVariable @Positive @NonNull Long commentId,
            @CurrentUser @NonNull User user) {
        LikeScrapToggleResponse response = postCommentLikeService.like(postId, commentId, user);
        return ResponseEntity.ok(CommonResponse.ok(response));
    }

    @Override
    @DeleteMapping("/posts/{postId}/comments/{commentId}/like")
    public ResponseEntity<CommonResponse<LikeScrapToggleResponse>> unlikeComment(
            @PathVariable @Positive @NonNull Long postId,
            @PathVariable @Positive @NonNull Long commentId,
            @CurrentUser @NonNull User user) {
        LikeScrapToggleResponse response = postCommentLikeService.unlike(postId, commentId, user);
        return ResponseEntity.ok(CommonResponse.ok(response));
    }
}
