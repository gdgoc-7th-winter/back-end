package com.project.algo.presentation.controller;

import com.project.algo.application.dto.AlgoLikeToggleResponse;
import com.project.algo.application.dto.AnswerCodePostCreateRequest;
import com.project.algo.application.dto.AnswerCodePostDetailResponse;
import com.project.algo.application.dto.AnswerCodePostListResponse;
import com.project.algo.application.dto.AnswerCodePostUpdateRequest;
import com.project.algo.application.dto.CodeRunRequest;
import com.project.algo.application.dto.CodeRunResponse;
import com.project.algo.application.dto.ProgrammingLanguageResponse;
import com.project.algo.application.service.AnswerCodePostCommandService;
import com.project.algo.application.service.AnswerCodePostLikeService;
import com.project.algo.application.service.AnswerCodePostQueryService;
import com.project.algo.application.service.CodeExecutionService;
import com.project.algo.application.service.ProgrammingLanguageQueryService;
import com.project.algo.presentation.swagger.AnswerCodePostControllerDocs;
import com.project.global.annotation.CurrentUser;
import com.project.global.response.CommonResponse;
import com.project.global.response.PageResponse;
import com.project.user.domain.entity.User;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/algo")
@Validated
@RequiredArgsConstructor
public class AnswerCodePostController implements AnswerCodePostControllerDocs {

    private final AnswerCodePostCommandService commandService;
    private final AnswerCodePostQueryService queryService;
    private final AnswerCodePostLikeService likeService;
    private final ProgrammingLanguageQueryService languageQueryService;
    private final CodeExecutionService codeExecutionService;

    /** 풀이 작성 시 언어 선택 목록 — 에디터 syntaxMode 포함 */
    @GetMapping("/answers/languages")
    public ResponseEntity<CommonResponse<List<ProgrammingLanguageResponse>>> getLanguages() {
        return ResponseEntity.ok(CommonResponse.ok(languageQueryService.getAll()));
    }

    @PostMapping("/answers/run")
    public ResponseEntity<CommonResponse<CodeRunResponse>> run(
            @RequestBody @Valid @NonNull CodeRunRequest request,
            @CurrentUser @NonNull User user) {

        return ResponseEntity.ok(CommonResponse.ok(codeExecutionService.run(request)));
    }

    @GetMapping("/{challengeId}/answers")
    public ResponseEntity<CommonResponse<PageResponse<AnswerCodePostListResponse>>> getList(
            @PathVariable @Positive @NonNull Long challengeId,
            @ParameterObject
            @PageableDefault(size = 20, sort = "likeCount", direction = Sort.Direction.DESC)
            @NonNull Pageable pageable,
            @CurrentUser @NonNull User user) {

        Page<AnswerCodePostListResponse> page = queryService.getList(challengeId, user, pageable);
        return ResponseEntity.ok(CommonResponse.ok(PageResponse.of(page)));
    }

    @GetMapping("/{challengeId}/answers/{answerId}")
    public ResponseEntity<CommonResponse<AnswerCodePostDetailResponse>> getDetail(
            @PathVariable @Positive @NonNull Long challengeId,
            @PathVariable @Positive @NonNull Long answerId,
            @CurrentUser @NonNull User user) {

        return ResponseEntity.ok(CommonResponse.ok(queryService.getDetail(challengeId, answerId, user)));
    }

    @PostMapping("/{challengeId}/answers")
    public ResponseEntity<CommonResponse<Map<String, Long>>> create(
            @PathVariable @Positive @NonNull Long challengeId,
            @RequestBody @Valid @NonNull AnswerCodePostCreateRequest request,
            @CurrentUser @NonNull User user) {

        Long answerId = commandService.create(challengeId, request, user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonResponse.ok(Map.of("answerId", answerId)));
    }

    @PatchMapping("/{challengeId}/answers/{answerId}")
    public ResponseEntity<CommonResponse<Void>> update(
            @PathVariable @Positive @NonNull Long challengeId,
            @PathVariable @Positive @NonNull Long answerId,
            @RequestBody @Valid @NonNull AnswerCodePostUpdateRequest request,
            @CurrentUser @NonNull User user) {

        commandService.update(answerId, request, user);
        return ResponseEntity.ok(CommonResponse.ok());
    }

    @DeleteMapping("/{challengeId}/answers/{answerId}")
    public ResponseEntity<CommonResponse<Void>> delete(
            @PathVariable @Positive @NonNull Long challengeId,
            @PathVariable @Positive @NonNull Long answerId,
            @CurrentUser @NonNull User user) {

        commandService.delete(answerId, user);
        return ResponseEntity.ok(CommonResponse.ok());
    }

    @PostMapping("/{challengeId}/answers/{answerId}/like")
    public ResponseEntity<CommonResponse<AlgoLikeToggleResponse>> toggleLike(
            @PathVariable @Positive @NonNull Long challengeId,
            @PathVariable @Positive @NonNull Long answerId,
            @CurrentUser @NonNull User user) {

        AlgoLikeToggleResponse response = likeService.toggle(challengeId, answerId, user);
        return ResponseEntity.ok(CommonResponse.ok(response));
    }
}
