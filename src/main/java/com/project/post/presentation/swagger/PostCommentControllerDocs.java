package com.project.post.presentation.swagger;

import com.project.post.application.dto.PostCommentRequest;
import com.project.post.application.dto.PostCommentResponse;
import com.project.global.response.CommonResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import com.project.user.domain.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

@Tag(name = "PostComment", description = "게시글 댓글 API")
public interface PostCommentControllerDocs {

    @Operation(summary = "댓글 작성", description = "게시글에 댓글을 작성합니다.")
    @ApiResponse(responseCode = "201", description = "생성됨")
    @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content(schema = @Schema(hidden = true)))
    ResponseEntity<CommonResponse<Long>> createComment(
            @Parameter(description = "게시글 ID") Long postId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "댓글 작성 요청") PostCommentRequest request,
            User user);

    @Operation(summary = "댓글 목록 조회", description = "게시글의 댓글 목록을 페이징하여 조회합니다.")
    @ApiResponse(responseCode = "200", description = "성공")
    ResponseEntity<CommonResponse<Page<PostCommentResponse>>> getComments(
            @Parameter(description = "게시글 ID") Long postId,
            Pageable pageable);

    @Operation(summary = "댓글 삭제", description = "댓글을 소프트 삭제합니다.")
    @ApiResponse(responseCode = "200", description = "성공")
    @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content(schema = @Schema(hidden = true)))
    ResponseEntity<CommonResponse<Void>> deleteComment(
            @Parameter(description = "게시글 ID") Long postId,
            @Parameter(description = "댓글 ID") Long commentId,
            User user);
}
