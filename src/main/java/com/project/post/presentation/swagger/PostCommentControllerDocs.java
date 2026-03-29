package com.project.post.presentation.swagger;

import com.project.global.annotation.OptionalSessionUser;
import com.project.global.response.CommonResponse;
import com.project.post.application.dto.LikeScrapToggleResponse;
import com.project.post.application.dto.PostCommentChildListResponse;
import com.project.post.application.dto.PostCommentCreateResponse;
import com.project.post.application.dto.PostCommentRequest;
import com.project.post.application.dto.PostCommentRootListResponse;
import com.project.post.domain.constants.PostConstants;
import com.project.user.domain.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Optional;

@Tag(name = "PostComment", description = "게시글 댓글 API")
public interface PostCommentControllerDocs {

    @Operation(
            summary = "댓글 작성",
            description = "지정한 게시글에 댓글을 남깁니다. 답글인 경우 요청 본문에 부모 댓글 ID를 넣습니다.")
    @ApiResponse(responseCode = "201", description = "생성됨")
    @ApiResponse(responseCode = "401", description = "인증 필요")
    ResponseEntity<CommonResponse<PostCommentCreateResponse>> createComment(
            @Parameter(description = "게시글 ID") @Positive @NonNull Long postId,
            @RequestBody @Valid @NonNull PostCommentRequest request,
            @Parameter(hidden = true) @NonNull User user);

    @Operation(
            summary = "최상위 댓글 목록 조회",
            description = "부모 댓글이 없는 최상위 댓글만 작성 순으로 조회합니다. "
                    + "커서는 첫 요청에서는 넣지 않고, 다음 페이지부터는 직전 응답에 있던 nextCursor를 cursor에 넣으면 됩니다. "
                    + "totalCommentCount는 그 게시글에 달린 댓글 전체 개수(최상위·답글 합)이며, 각 최상위 댓글 아래 답글은 일부만 미리보기로 내려갑니다. "
                    + "hasMoreReplies가 true이면 `GET /posts/{postId}/comments/{부모 댓글 ID}/comments`로 나머지 답글을 이어서 조회합니다. "
                    + "이때 path의 부모 댓글 ID는 최상위 댓글만 허용합니다.")
    @ApiResponse(responseCode = "200", description = "성공")
    @ApiResponse(responseCode = "404", description = "게시글 없음")
    ResponseEntity<CommonResponse<PostCommentRootListResponse>> getComments(
            @Parameter(description = "게시글 ID") @Positive @NonNull Long postId,
            @Parameter(description = "직전 응답의 nextCursor. 첫 조회에서는 생략합니다.") @Nullable String cursor,
            @Parameter(description = "한 번에 가져올 최상위 댓글 개수(최대 100)") @Positive @Max(PostConstants.MAX_COMMENT_CURSOR_PAGE_SIZE) int size,
            @Parameter(hidden = true) @OptionalSessionUser Optional<User> optionalViewer);

    @Operation(
            summary = "부모 댓글의 답글 목록 조회",
            description = "특정 부모 댓글에 달린 답글만 작성 순으로 조회합니다. "
                    + "path의 부모 댓글 ID는 최상위 댓글이어야 하며, 커서·페이지 크기 규칙은 위의 최상위 댓글 목록 조회와 같습니다.")
    @ApiResponse(responseCode = "200", description = "성공")
    @ApiResponse(responseCode = "400", description = "커서가 잘못되었거나 부모 댓글 ID가 최상위가 아닌 경우 등")
    @ApiResponse(responseCode = "404", description = "게시글 또는 댓글 없음")
    ResponseEntity<CommonResponse<PostCommentChildListResponse>> getChildComments(
            @Parameter(description = "게시글 ID") @Positive @NonNull Long postId,
            @Parameter(description = "부모 댓글 ID(답글을 묶는 최상위 댓글)") @Positive @NonNull Long parentCommentId,
            @Parameter(description = "직전 응답의 nextCursor. 첫 조회에서는 생략합니다.") @Nullable String cursor,
            @Parameter(description = "한 번에 가져올 답글 개수(최대 100)") @Positive @Max(PostConstants.MAX_COMMENT_CURSOR_PAGE_SIZE) int size,
            @Parameter(hidden = true) @OptionalSessionUser Optional<User> optionalViewer);

    @Operation(
            summary = "댓글 삭제",
            description = "본인이 작성한 댓글을 소프트 삭제합니다.")
    @ApiResponse(responseCode = "200", description = "성공")
    @ApiResponse(responseCode = "401", description = "인증 필요")
    ResponseEntity<CommonResponse<Void>> deleteComment(
            @Parameter(description = "게시글 ID") @Positive @NonNull Long postId,
            @Parameter(description = "삭제할 댓글 ID") @Positive @NonNull Long commentId,
            @Parameter(hidden = true) @NonNull User user);

    @Operation(
            summary = "댓글 좋아요",
            description = "댓글에 좋아요를 누릅니다.")
    @ApiResponse(responseCode = "200", description = "성공")
    @ApiResponse(responseCode = "401", description = "인증 필요")
    ResponseEntity<CommonResponse<LikeScrapToggleResponse>> likeComment(
            @Parameter(description = "게시글 ID") @Positive @NonNull Long postId,
            @Parameter(description = "댓글 ID") @Positive @NonNull Long commentId,
            @Parameter(hidden = true) @NonNull User user);

    @Operation(
            summary = "댓글 좋아요 취소",
            description = "댓글에 눌렀던 좋아요를 취소합니다.")
    @ApiResponse(responseCode = "200", description = "성공")
    @ApiResponse(responseCode = "401", description = "인증 필요")
    ResponseEntity<CommonResponse<LikeScrapToggleResponse>> unlikeComment(
            @Parameter(description = "게시글 ID") @Positive @NonNull Long postId,
            @Parameter(description = "댓글 ID") @Positive @NonNull Long commentId,
            @Parameter(hidden = true) @NonNull User user);
}
