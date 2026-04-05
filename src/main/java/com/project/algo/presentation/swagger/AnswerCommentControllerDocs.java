package com.project.algo.presentation.swagger;

import com.project.algo.application.dto.AnswerCommentCreateRequest;
import com.project.algo.application.dto.AnswerCommentResponse;
import com.project.algo.application.dto.AnswerCommentUpdateRequest;
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

import java.util.Map;

@Tag(name = "AnswerComment", description = "풀이 코멘트 API")
public interface AnswerCommentControllerDocs {

    @Operation(
            summary = "코멘트 목록 조회",
            description = "특정 풀이에 달린 코멘트 목록을 등록 순(오름차순)으로 페이징 조회합니다. "
                    + "해당 문제에 풀이를 제출한 사용자만 조회할 수 있습니다. "
                    + "소프트 삭제된 코멘트는 content가 null로 마스킹되어 포함됩니다."
    )
    @ApiResponse(responseCode = "200", description = "성공")
    @ApiResponse(responseCode = "401", description = "인증 필요")
    @ApiResponse(responseCode = "403", description = "풀이를 먼저 제출해야 조회 가능")
    @ApiResponse(responseCode = "404", description = "문제 또는 풀이 없음")
    ResponseEntity<CommonResponse<PageResponse<AnswerCommentResponse>>> getList(
            @Parameter(description = "문제 ID") @Positive @NonNull Long challengeId,
            @Parameter(description = "풀이 ID") @Positive @NonNull Long answerId,
            @ParameterObject @NonNull Pageable pageable,
            @Parameter(hidden = true) @NonNull User user
    );

    @Operation(
            summary = "코멘트 등록",
            description = "특정 풀이에 코멘트를 등록합니다. "
                    + "referencedLines로 코드의 특정 줄을 참조할 수 있으며, "
                    + "commentTag로 코멘트 유형(COMPLIMENT·SUGGEST·QUESTION)을 지정할 수 있습니다. "
                    + "해당 문제에 풀이를 제출한 사용자만 코멘트를 작성할 수 있습니다."
    )
    @ApiResponse(responseCode = "201", description = "생성됨")
    @ApiResponse(responseCode = "401", description = "인증 필요")
    @ApiResponse(responseCode = "403", description = "풀이를 먼저 제출해야 코멘트 가능")
    @ApiResponse(responseCode = "404", description = "문제 또는 풀이 없음")
    ResponseEntity<CommonResponse<Map<String, Long>>> create(
            @Parameter(description = "문제 ID") @Positive @NonNull Long challengeId,
            @Parameter(description = "풀이 ID") @Positive @NonNull Long answerId,
            @RequestBody(description = "코멘트 등록 요청") @Valid @NonNull AnswerCommentCreateRequest request,
            @Parameter(hidden = true) @NonNull User user
    );

    @Operation(
            summary = "코멘트 수정",
            description = "자신이 작성한 코멘트의 내용 또는 태그를 수정합니다. 작성자 본인만 수정할 수 있으며 ADMIN도 타인의 코멘트는 수정할 수 없습니다."
    )
    @ApiResponse(responseCode = "200", description = "성공")
    @ApiResponse(responseCode = "401", description = "인증 필요")
    @ApiResponse(responseCode = "403", description = "권한 없음 (작성자 본인만 수정 가능)")
    @ApiResponse(responseCode = "404", description = "코멘트 없음")
    ResponseEntity<CommonResponse<Void>> update(
            @Parameter(description = "문제 ID") @Positive @NonNull Long challengeId,
            @Parameter(description = "풀이 ID") @Positive @NonNull Long answerId,
            @Parameter(description = "코멘트 ID") @Positive @NonNull Long commentId,
            @RequestBody(description = "코멘트 수정 요청") @Valid @NonNull AnswerCommentUpdateRequest request,
            @Parameter(hidden = true) @NonNull User user
    );

    @Operation(
            summary = "코멘트 삭제",
            description = "코멘트를 소프트 삭제합니다. 삭제 후 content는 null로 마스킹됩니다. "
                    + "ADMIN은 타인의 코멘트도 삭제할 수 있습니다."
    )
    @ApiResponse(responseCode = "200", description = "성공")
    @ApiResponse(responseCode = "401", description = "인증 필요")
    @ApiResponse(responseCode = "403", description = "권한 없음 (작성자 본인 또는 ADMIN만 삭제 가능)")
    @ApiResponse(responseCode = "404", description = "코멘트 없음")
    ResponseEntity<CommonResponse<Void>> delete(
            @Parameter(description = "문제 ID") @Positive @NonNull Long challengeId,
            @Parameter(description = "풀이 ID") @Positive @NonNull Long answerId,
            @Parameter(description = "코멘트 ID") @Positive @NonNull Long commentId,
            @Parameter(hidden = true) @NonNull User user
    );
}
