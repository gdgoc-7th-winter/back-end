package com.project.contribution.presentation.swagger;

import com.project.contribution.presentation.dto.ScoreCreateRequest;
import com.project.contribution.presentation.dto.ScoreResponse;
import com.project.contribution.presentation.dto.ScoreUpdateRequest;
import com.project.global.response.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;

@Tag(name = "ContributionScore", description = "기여도 점수 항목 API")
public interface ContributionScoreControllerDocs {

    @Operation(summary = "점수 항목 생성", description = "새 기여도 점수 항목을 생성합니다.")
    @ApiResponse(responseCode = "201", description = "생성됨")
    @ApiResponse(responseCode = "403", description = "접근 권한 없음", content = @Content(schema = @Schema(hidden = true)))
    ResponseEntity<CommonResponse<ScoreResponse>> addScore(
            @RequestBody(description = "점수 항목 생성 요청") ScoreCreateRequest request);

    @Operation(summary = "점수 항목 수정", description = "기여도 점수 항목을 수정합니다.")
    @ApiResponse(responseCode = "200", description = "성공")
    @ApiResponse(responseCode = "403", description = "접근 권한 없음", content = @Content(schema = @Schema(hidden = true)))
    ResponseEntity<CommonResponse<ScoreResponse>> editScore(
            @Parameter(description = "점수 항목 ID") @Positive @NonNull Long id,
            @RequestBody(description = "점수 항목 수정 요청") ScoreUpdateRequest request);

    @Operation(summary = "점수 항목 조회", description = "기여도 점수 항목을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "성공")
    @ApiResponse(responseCode = "403", description = "접근 권한 없음", content = @Content(schema = @Schema(hidden = true)))
    ResponseEntity<CommonResponse<ScoreResponse>> getScore(
            @Parameter(description = "점수 항목 ID") @Positive @NonNull Long id);

    @Operation(summary = "점수 항목 삭제", description = "기여도 점수 항목을 삭제합니다.")
    @ApiResponse(responseCode = "200", description = "성공")
    @ApiResponse(responseCode = "403", description = "접근 권한 없음", content = @Content(schema = @Schema(hidden = true)))
    ResponseEntity<CommonResponse<Void>> deleteScore(
            @Parameter(description = "점수 항목 ID") @Positive @NonNull Long id);
}
