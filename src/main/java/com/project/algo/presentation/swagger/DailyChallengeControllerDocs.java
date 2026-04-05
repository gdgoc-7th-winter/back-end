package com.project.algo.presentation.swagger;

import com.project.algo.application.dto.DailyChallengeCreateRequest;
import com.project.algo.application.dto.DailyChallengeDetailResponse;
import com.project.algo.application.dto.DailyChallengeListResponse;
import com.project.algo.application.dto.DailyChallengeUpdateRequest;
import com.project.algo.domain.enums.AlgorithmTag;
import com.project.global.response.CommonResponse;
import com.project.global.response.PageResponse;
import com.project.user.domain.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Map;

@Tag(name = "DailyChallenge", description = "코딩테스트 문제 게시판 API")
public interface DailyChallengeControllerDocs {

    @Operation(
            summary = "문제 목록 조회",
            description = "코딩테스트 문제 목록을 페이징하여 조회합니다. "
                    + "키워드(제목·문제번호 부분 일치)와 알고리즘 태그 필터를 복합 적용할 수 있습니다. "
                    + "기본 정렬은 등록일 내림차순입니다."
    )
    @ApiResponse(responseCode = "200", description = "성공")
    ResponseEntity<CommonResponse<PageResponse<DailyChallengeListResponse>>> getList(
            @Parameter(description = "검색 키워드 (제목·문제번호 부분 일치)") String keyword,
            @Parameter(description = "알고리즘 태그 필터 (복수 가능, OR 조건)") List<AlgorithmTag> algorithmTags,
            @ParameterObject @NonNull Pageable pageable
    );

    @Operation(
            summary = "문제 상세 조회",
            description = "문제 ID로 코딩테스트 문제의 상세 정보를 조회합니다. "
                    + "전일 MVP(1~3위) 정보가 mvps 필드에 포함됩니다."
    )
    @ApiResponse(responseCode = "200", description = "성공")
    @ApiResponse(responseCode = "404", description = "문제 없음")
    ResponseEntity<CommonResponse<DailyChallengeDetailResponse>> getDetail(
            @Parameter(description = "문제 ID") @Positive @NonNull Long challengeId
    );

    @Operation(
            summary = "문제 등록",
            description = "새 코딩테스트 문제를 등록합니다. "
                    + "프로필 미설정 상태(DUMMY)를 제외한 모든 로그인 회원이 등록할 수 있습니다."
    )
    @ApiResponse(responseCode = "201", description = "생성됨")
    @ApiResponse(responseCode = "401", description = "인증 필요")
    @ApiResponse(responseCode = "403", description = "권한 없음 (프로필 미설정 회원(DUMMY) 불가)")
    ResponseEntity<CommonResponse<Map<String, Long>>> create(
            @RequestBody(description = "문제 등록 요청") @Valid @NonNull DailyChallengeCreateRequest request,
            @Parameter(hidden = true) @NonNull User user
    );

    @Operation(
            summary = "문제 수정",
            description = "코딩테스트 문제를 수정합니다. 작성자 본인 또는 ADMIN만 수정할 수 있습니다. "
                    + "모든 필드는 선택 입력이며, null로 전송하면 해당 필드는 변경되지 않습니다."
    )
    @ApiResponse(responseCode = "200", description = "성공")
    @ApiResponse(responseCode = "401", description = "인증 필요")
    @ApiResponse(responseCode = "403", description = "권한 없음 (작성자 본인 또는 ADMIN만 수정 가능)")
    @ApiResponse(responseCode = "404", description = "문제 없음")
    ResponseEntity<CommonResponse<Void>> update(
            @Parameter(description = "문제 ID") @Positive @NonNull Long challengeId,
            @RequestBody(description = "문제 수정 요청") @Valid @NonNull DailyChallengeUpdateRequest request,
            @Parameter(hidden = true) @NonNull User user
    );

    @Operation(
            summary = "문제 삭제",
            description = "코딩테스트 문제를 소프트 삭제합니다. 작성자 본인 또는 ADMIN만 삭제할 수 있습니다."
    )
    @ApiResponse(responseCode = "200", description = "성공")
    @ApiResponse(responseCode = "401", description = "인증 필요")
    @ApiResponse(responseCode = "403", description = "권한 없음 (작성자 본인 또는 ADMIN만 삭제 가능)")
    @ApiResponse(responseCode = "404", description = "문제 없음")
    ResponseEntity<CommonResponse<Void>> delete(
            @Parameter(description = "문제 ID") @Positive @NonNull Long challengeId,
            @Parameter(hidden = true) @NonNull User user
    );
}
