package com.project.ranking.presentation.swagger;

import com.project.global.response.CommonResponse;
import com.project.ranking.application.dto.RankingListResponse;
import com.project.ranking.application.dto.RankingMeResponse;
import com.project.ranking.domain.RankingPeriodType;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Ranking", description = "랭킹 스냅샷 조회 API")
public interface RankingControllerDocs {

    @Operation(
            summary = "랭킹 스냅샷 목록 조회",
            description = "페이징된 랭킹 목록과 집계 메타를 반환합니다. period_key를 생략하면 ALL_TIME은 ALL, 주간·월간은 직전 완료 기간을 적용합니다. "
                    + "스냅샷이 없으면 빈 목록을 반환합니다. 로그인 시 본인 행에 isMe를 표시합니다.")
    @ApiResponse(responseCode = "200", description = "성공")
    ResponseEntity<CommonResponse<RankingListResponse>> list(
            @Parameter(description = "집계 기간 유형", required = true, example = "WEEKLY")
            @RequestParam("period_type") RankingPeriodType periodType,
            @Parameter(description = "기간 키 · 생략 시 서버 기본")
            @RequestParam(value = "period_key", required = false) String periodKey,
            @Parameter(description = "페이지 번호(0부터)", example = "0")
            @RequestParam(value = "page", defaultValue = "0") int page,
            @Parameter(description = "페이지 크기 · 생략 시 기본값")
            @RequestParam(value = "size", required = false) Integer size,
            @Parameter(hidden = true) HttpSession session);

    @Operation(
            summary = "내 랭킹 조회",
            description = "로그인 사용자의 해당 기간 랭킹을 반환합니다. 스냅샷이 없거나 포함되지 않으면 404를 반환합니다.")
    @ApiResponse(responseCode = "200", description = "성공")
    @ApiResponse(responseCode = "401", description = "세션 만료", content = @Content(schema = @Schema(hidden = true)))
    @ApiResponse(responseCode = "404", description = "리소스를 찾을 수 없음", content = @Content(schema = @Schema(hidden = true)))
    ResponseEntity<CommonResponse<RankingMeResponse>> myRank(
            @Parameter(description = "집계 기간 유형", required = true, example = "MONTHLY")
            @RequestParam("period_type") RankingPeriodType periodType,
            @Parameter(description = "기간 키 · 목록 조회와 동일(생략 시 서버 기본)")
            @RequestParam(value = "period_key", required = false) String periodKey,
            @Parameter(hidden = true) HttpSession session);
}
