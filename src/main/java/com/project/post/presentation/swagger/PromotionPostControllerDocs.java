package com.project.post.presentation.swagger;

import com.project.global.response.CommonResponse;
import com.project.post.application.dto.PromotionPost.PromotionPostCreateRequest;
import com.project.post.application.dto.PromotionPost.PromotionPostCreateResponse;
import com.project.post.application.dto.PromotionPost.PromotionPostDetailResponse;
import com.project.post.application.dto.PromotionPost.PromotionPostListResponse;
import com.project.post.application.dto.PromotionPost.PromotionPostUpdateRequest;
import com.project.post.domain.enums.PromotionCategory;
import com.project.user.domain.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;

@Tag(name = "Promotion Post", description = "홍보글 API")
public interface PromotionPostControllerDocs {

    @Operation(summary = "홍보글 작성", description = "새 홍보글을 작성합니다.")
    @ApiResponse(responseCode = "201", description = "생성됨")
    @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content(schema = @Schema(hidden = true)))
    ResponseEntity<CommonResponse<PromotionPostCreateResponse>> create(
            @RequestBody(description = "홍보글 작성 요청") @Valid @NonNull PromotionPostCreateRequest request,
            @Parameter(hidden = true) @NonNull User user
    );

    @Operation(summary = "홍보글 상세 조회", description = "홍보글 ID로 상세 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "성공")
    ResponseEntity<CommonResponse<PromotionPostDetailResponse>> getDetail(
            @Parameter(description = "홍보글 ID") @Positive @NonNull Long postId
    );

    @Operation(summary = "홍보글 목록 조회", description = "홍보글 목록을 페이징하여 조회합니다. 카테고리로 필터링할 수 있습니다.")
    @ApiResponse(responseCode = "200", description = "성공")
    ResponseEntity<CommonResponse<Page<PromotionPostListResponse>>> getList(
            @Parameter(description = "홍보 카테고리") PromotionCategory category,
            @Parameter(description = "페이지 정보") @NonNull Pageable pageable
    );

    @Operation(summary = "홍보글 수정", description = "홍보글을 수정합니다.")
    @ApiResponse(responseCode = "200", description = "성공")
    @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content(schema = @Schema(hidden = true)))
    ResponseEntity<CommonResponse<Void>> update(
            @Parameter(description = "홍보글 ID") @Positive @NonNull Long id,
            @RequestBody(description = "홍보글 수정 요청") @Valid @NonNull PromotionPostUpdateRequest request,
            @Parameter(hidden = true) @NonNull User user
    );

    @Operation(summary = "홍보글 삭제", description = "홍보글을 소프트 삭제합니다.")
    @ApiResponse(responseCode = "200", description = "성공")
    @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content(schema = @Schema(hidden = true)))
    ResponseEntity<CommonResponse<Void>> delete(
            @Parameter(description = "홍보글 ID") @Positive @NonNull Long id,
            @Parameter(hidden = true) @NonNull User user
    );
}