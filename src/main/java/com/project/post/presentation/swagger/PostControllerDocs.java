package com.project.post.presentation.swagger;

import com.project.post.application.dto.LikeScrapToggleResponse;
import com.project.post.application.dto.PostCreateRequest;
import com.project.post.application.dto.PostDetailResponse;
import com.project.post.application.dto.PostListResponse;
import com.project.post.application.dto.PostUpdateRequest;
import com.project.global.response.CommonResponse;
import jakarta.validation.constraints.Positive;
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
import org.springframework.lang.NonNull;

@Tag(name = "Post", description = "게시글 API")
public interface PostControllerDocs {

    @Operation(summary = "게시글 목록 조회", description = "게시판 코드로 게시글 목록을 페이징하여 조회합니다.")
    @ApiResponse(responseCode = "200", description = "성공")
    ResponseEntity<CommonResponse<Page<PostListResponse>>> getList(
            @Parameter(description = "게시판 코드") @NonNull String code,
            @NonNull Pageable pageable);

    @Operation(summary = "게시글 상세 조회", description = "게시글 ID로 상세 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "성공")
    ResponseEntity<CommonResponse<PostDetailResponse>> getDetail(
            @Parameter(description = "게시글 ID") @Positive @NonNull Long id);

    @Operation(summary = "게시글 조회수 증가", description = "게시글 조회수를 1 증가시킵니다.")
    @ApiResponse(responseCode = "200", description = "성공")
    ResponseEntity<CommonResponse<Void>> increaseViewCount(
            @Parameter(description = "게시글 ID") @Positive @NonNull Long id);

    @Operation(summary = "게시글 작성", description = "새 게시글을 작성합니다.")
    @ApiResponse(responseCode = "201", description = "생성됨")
    @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content(schema = @Schema(hidden = true)))
    ResponseEntity<CommonResponse<Long>> create(
            @Parameter(description = "게시판 코드") @NonNull String code,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "게시글 작성 요청") @NonNull PostCreateRequest request,
            @Parameter(hidden = true) @NonNull User user);

    @Operation(summary = "게시글 수정", description = "게시글을 수정합니다.")
    @ApiResponse(responseCode = "200", description = "성공")
    @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content(schema = @Schema(hidden = true)))
    ResponseEntity<CommonResponse<Void>> update(
            @Parameter(description = "게시글 ID") @Positive @NonNull Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "게시글 수정 요청") @NonNull PostUpdateRequest request,
            @Parameter(hidden = true) @NonNull User user);

    @Operation(summary = "게시글 삭제", description = "게시글을 소프트 삭제합니다.")
    @ApiResponse(responseCode = "200", description = "성공")
    @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content(schema = @Schema(hidden = true)))
    ResponseEntity<CommonResponse<Void>> delete(
            @Parameter(description = "게시글 ID") @Positive @NonNull Long id,
            @Parameter(hidden = true) @NonNull User user);

    @Operation(summary = "좋아요 추가", description = "게시글에 좋아요를 추가합니다.")
    @ApiResponse(responseCode = "200", description = "성공")
    @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content(schema = @Schema(hidden = true)))
    ResponseEntity<CommonResponse<LikeScrapToggleResponse>> like(
            @Parameter(description = "게시글 ID") @Positive @NonNull Long id,
            @Parameter(hidden = true) @NonNull User user);

    @Operation(summary = "좋아요 취소", description = "게시글 좋아요를 취소합니다.")
    @ApiResponse(responseCode = "200", description = "성공")
    @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content(schema = @Schema(hidden = true)))
    ResponseEntity<CommonResponse<LikeScrapToggleResponse>> unlike(
            @Parameter(description = "게시글 ID") @Positive @NonNull Long id,
            @Parameter(hidden = true) @NonNull User user);

    @Operation(summary = "스크랩 추가", description = "게시글을 스크랩합니다.")
    @ApiResponse(responseCode = "200", description = "성공")
    @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content(schema = @Schema(hidden = true)))
    ResponseEntity<CommonResponse<LikeScrapToggleResponse>> scrap(
            @Parameter(description = "게시글 ID") @Positive @NonNull Long id,
            @Parameter(hidden = true) @NonNull User user);

    @Operation(summary = "스크랩 취소", description = "게시글 스크랩을 취소합니다.")
    @ApiResponse(responseCode = "200", description = "성공")
    @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content(schema = @Schema(hidden = true)))
    ResponseEntity<CommonResponse<LikeScrapToggleResponse>> unscrap(
            @Parameter(description = "게시글 ID") @Positive @NonNull Long id,
            @Parameter(hidden = true) @NonNull User user);
}
