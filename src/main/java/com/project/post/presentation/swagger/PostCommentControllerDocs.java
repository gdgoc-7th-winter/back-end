package com.project.post.presentation.swagger;

import com.project.global.annotation.OptionalSessionUser;
import com.project.global.response.CommonResponse;
import com.project.global.response.PageResponse;
import com.project.post.application.dto.LikeScrapToggleResponse;
import com.project.post.application.dto.PostCommentCreateResponse;
import com.project.post.application.dto.PostCommentRequest;
import com.project.post.application.dto.PostCommentResponse;
import com.project.user.domain.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;

import java.util.Optional;

@Tag(name = "PostComment", description = "게시글 댓글 API")
public interface PostCommentControllerDocs {

    @Operation(summary = "댓글 작성", description = "게시글에 댓글을 작성합니다.")
    @ApiResponse(responseCode = "201", description = "생성됨")
    @ApiResponse(responseCode = "401", description = "인증 필요")
    ResponseEntity<CommonResponse<PostCommentCreateResponse>> createComment(
            @Parameter(description = "게시글 ID") @Positive @NonNull Long postId,
            @RequestBody(description = "댓글 작성 요청") @Valid @NonNull PostCommentRequest request,
            @Parameter(hidden = true) @NonNull User user);

    @Operation(summary = "댓글 목록 조회", description = "게시글의 댓글 목록을 페이징하여 조회합니다. 각 루트 댓글의 답글은 최대 20개까지 반환되며, 초과 시 hasMoreReplies=true로 표시됩니다. 로그인 시 viewer.liked / viewer.isAuthor 에 현재 사용자 기준 상태가 포함됩니다.")
    @ApiResponse(responseCode = "200", description = "성공")
    ResponseEntity<CommonResponse<PageResponse<PostCommentResponse>>> getComments(
            @Parameter(description = "게시글 ID") @Positive @NonNull Long postId,
            @NonNull Pageable pageable,
            @Parameter(hidden = true) @OptionalSessionUser Optional<User> optionalViewer);

    @Operation(summary = "댓글 삭제", description = "댓글을 소프트 삭제합니다.")
    @ApiResponse(responseCode = "200", description = "성공")
    @ApiResponse(responseCode = "401", description = "인증 필요")
    ResponseEntity<CommonResponse<Void>> deleteComment(
            @Parameter(description = "게시글 ID") @Positive @NonNull Long postId,
            @Parameter(description = "댓글 ID") @Positive @NonNull Long commentId,
            @Parameter(hidden = true) @NonNull User user);

    @Operation(summary = "댓글 좋아요 추가", description = "댓글에 좋아요를 추가합니다.")
    @ApiResponse(responseCode = "200", description = "성공")
    @ApiResponse(responseCode = "401", description = "인증 필요")
    ResponseEntity<CommonResponse<LikeScrapToggleResponse>> likeComment(
            @Parameter(description = "게시글 ID") @Positive @NonNull Long postId,
            @Parameter(description = "댓글 ID") @Positive @NonNull Long commentId,
            @Parameter(hidden = true) @NonNull User user);

    @Operation(summary = "댓글 좋아요 취소", description = "댓글 좋아요를 취소합니다.")
    @ApiResponse(responseCode = "200", description = "성공")
    @ApiResponse(responseCode = "401", description = "인증 필요")
    ResponseEntity<CommonResponse<LikeScrapToggleResponse>> unlikeComment(
            @Parameter(description = "게시글 ID") @Positive @NonNull Long postId,
            @Parameter(description = "댓글 ID") @Positive @NonNull Long commentId,
            @Parameter(hidden = true) @NonNull User user);
}
