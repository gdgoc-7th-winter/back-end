package com.project.algo.presentation.swagger;

import com.project.algo.application.dto.AlgoLikeToggleResponse;
import com.project.algo.application.dto.AnswerCodePostCreateRequest;
import com.project.algo.application.dto.AnswerCodePostDetailResponse;
import com.project.algo.application.dto.AnswerCodePostListResponse;
import com.project.algo.application.dto.AnswerCodePostUpdateRequest;
import com.project.algo.application.dto.CodeRunRequest;
import com.project.algo.application.dto.CodeRunResponse;
import com.project.algo.application.dto.ProgrammingLanguageResponse;
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

@Tag(name = "AnswerCodePost", description = "풀이 코드 게시글 API")
public interface AnswerCodePostControllerDocs {

    @Operation(
            summary = "지원 언어 목록 조회",
            description = "풀이 작성 시 선택 가능한 프로그래밍 언어 목록을 반환합니다. "
                    + "각 언어의 에디터 syntaxMode와 파일 확장자가 포함됩니다."
    )
    @ApiResponse(responseCode = "200", description = "성공")
    ResponseEntity<CommonResponse<List<ProgrammingLanguageResponse>>> getLanguages();

    @Operation(
            summary = "코드 실행",
            description = "제출한 코드를 Judge 엔진을 통해 실행하고 stdout/stderr 결과를 반환합니다. "
                    + "테스트케이스 없이 자유 실행만 지원하며, 정답/오답 판별은 불가합니다. "
                    + "stdin에 표준 입력을 넣을 수 있습니다."
    )
    @ApiResponse(responseCode = "200", description = "성공")
    @ApiResponse(responseCode = "401", description = "인증 필요")
    ResponseEntity<CommonResponse<CodeRunResponse>> run(
            @RequestBody(description = "코드 실행 요청") @Valid @NonNull CodeRunRequest request,
            @Parameter(hidden = true) @NonNull User user
    );

    @Operation(
            summary = "풀이 목록 조회",
            description = "특정 문제에 등록된 풀이 목록을 좋아요 내림차순으로 페이징 조회합니다."
    )
    @ApiResponse(responseCode = "200", description = "성공")
    @ApiResponse(responseCode = "401", description = "인증 필요")
    @ApiResponse(responseCode = "404", description = "문제 없음")
    ResponseEntity<CommonResponse<PageResponse<AnswerCodePostListResponse>>> getList(
            @Parameter(description = "문제 ID") @Positive @NonNull Long challengeId,
            @ParameterObject @NonNull Pageable pageable,
            @Parameter(hidden = true) @NonNull User user
    );

    @Operation(
            summary = "풀이 상세 조회",
            description = "특정 풀이의 상세 정보를 조회합니다. 코드 전문, 설명, 알고리즘 태그 등이 포함됩니다."
    )
    @ApiResponse(responseCode = "200", description = "성공")
    @ApiResponse(responseCode = "401", description = "인증 필요")
    @ApiResponse(responseCode = "404", description = "문제 또는 풀이 없음")
    ResponseEntity<CommonResponse<AnswerCodePostDetailResponse>> getDetail(
            @Parameter(description = "문제 ID") @Positive @NonNull Long challengeId,
            @Parameter(description = "풀이 ID") @Positive @NonNull Long answerId,
            @Parameter(hidden = true) @NonNull User user
    );

    @Operation(
            summary = "풀이 등록",
            description = "특정 문제에 풀이를 등록합니다. 문제당 1인 1회만 등록 가능합니다. "
                    + "소프트 삭제 후 재제출은 허용됩니다."
    )
    @ApiResponse(responseCode = "201", description = "생성됨")
    @ApiResponse(responseCode = "400", description = "이미 풀이를 제출한 경우 (중복 제출 불가)")
    @ApiResponse(responseCode = "401", description = "인증 필요")
    @ApiResponse(responseCode = "404", description = "문제 없음")
    ResponseEntity<CommonResponse<Map<String, Long>>> create(
            @Parameter(description = "문제 ID") @Positive @NonNull Long challengeId,
            @RequestBody(description = "풀이 등록 요청") @Valid @NonNull AnswerCodePostCreateRequest request,
            @Parameter(hidden = true) @NonNull User user
    );

    @Operation(
            summary = "풀이 수정",
            description = "자신이 작성한 풀이를 수정합니다. 작성자 본인만 수정할 수 있으며 ADMIN도 타인의 풀이는 수정할 수 없습니다. "
                    + "null로 전송한 필드는 변경되지 않습니다."
    )
    @ApiResponse(responseCode = "200", description = "성공")
    @ApiResponse(responseCode = "401", description = "인증 필요")
    @ApiResponse(responseCode = "403", description = "권한 없음 (작성자 본인만 수정 가능)")
    @ApiResponse(responseCode = "404", description = "풀이 없음")
    ResponseEntity<CommonResponse<Void>> update(
            @Parameter(description = "문제 ID") @Positive @NonNull Long challengeId,
            @Parameter(description = "풀이 ID") @Positive @NonNull Long answerId,
            @RequestBody(description = "풀이 수정 요청") @Valid @NonNull AnswerCodePostUpdateRequest request,
            @Parameter(hidden = true) @NonNull User user
    );

    @Operation(
            summary = "풀이 삭제",
            description = "자신이 작성한 풀이를 소프트 삭제합니다. ADMIN은 타인의 풀이도 삭제할 수 있습니다."
    )
    @ApiResponse(responseCode = "200", description = "성공")
    @ApiResponse(responseCode = "401", description = "인증 필요")
    @ApiResponse(responseCode = "403", description = "권한 없음 (작성자 본인 또는 ADMIN만 삭제 가능)")
    @ApiResponse(responseCode = "404", description = "풀이 없음")
    ResponseEntity<CommonResponse<Void>> delete(
            @Parameter(description = "문제 ID") @Positive @NonNull Long challengeId,
            @Parameter(description = "풀이 ID") @Positive @NonNull Long answerId,
            @Parameter(hidden = true) @NonNull User user
    );

    @Operation(
            summary = "풀이 좋아요 토글",
            description = "풀이에 좋아요를 토글합니다. 좋아요가 없으면 추가, 있으면 취소합니다."
    )
    @ApiResponse(responseCode = "200", description = "성공")
    @ApiResponse(responseCode = "401", description = "인증 필요")
    @ApiResponse(responseCode = "404", description = "풀이 없음")
    ResponseEntity<CommonResponse<AlgoLikeToggleResponse>> toggleLike(
            @Parameter(description = "문제 ID") @Positive @NonNull Long challengeId,
            @Parameter(description = "풀이 ID") @Positive @NonNull Long answerId,
            @Parameter(hidden = true) @NonNull User user
    );
}
