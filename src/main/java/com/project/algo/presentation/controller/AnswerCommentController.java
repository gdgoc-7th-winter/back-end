package com.project.algo.presentation.controller;

import com.project.algo.application.dto.AnswerCommentCreateRequest;
import com.project.algo.application.dto.AnswerCommentResponse;
import com.project.algo.application.dto.AnswerCommentUpdateRequest;
import com.project.algo.application.service.AnswerCommentCommandService;
import com.project.algo.application.service.AnswerCommentQueryService;
import com.project.algo.presentation.swagger.AnswerCommentControllerDocs;
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

import java.util.Map;

@RestController
@RequestMapping("/api/v1/algo/{challengeId}/answers/{answerId}/comments")
@Validated
@RequiredArgsConstructor
public class AnswerCommentController implements AnswerCommentControllerDocs {

    private final AnswerCommentCommandService commandService;
    private final AnswerCommentQueryService queryService;

    @GetMapping
    public ResponseEntity<CommonResponse<PageResponse<AnswerCommentResponse>>> getList(
            @PathVariable @Positive @NonNull Long challengeId,
            @PathVariable @Positive @NonNull Long answerId,
            @ParameterObject
            @PageableDefault(size = 30, sort = "createdAt", direction = Sort.Direction.ASC)
            @NonNull Pageable pageable,
            @CurrentUser @NonNull User user) {

        Page<AnswerCommentResponse> page = queryService.getList(challengeId, answerId, user, pageable);
        return ResponseEntity.ok(CommonResponse.ok(PageResponse.of(page)));
    }

    @PostMapping
    public ResponseEntity<CommonResponse<Map<String, Long>>> create(
            @PathVariable @Positive @NonNull Long challengeId,
            @PathVariable @Positive @NonNull Long answerId,
            @RequestBody @Valid @NonNull AnswerCommentCreateRequest request,
            @CurrentUser @NonNull User user) {

        Long commentId = commandService.create(answerId, request, user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonResponse.ok(Map.of("commentId", commentId)));
    }

    @PatchMapping("/{commentId}")
    public ResponseEntity<CommonResponse<Void>> update(
            @PathVariable @Positive @NonNull Long challengeId,
            @PathVariable @Positive @NonNull Long answerId,
            @PathVariable @Positive @NonNull Long commentId,
            @RequestBody @Valid @NonNull AnswerCommentUpdateRequest request,
            @CurrentUser @NonNull User user) {

        commandService.update(commentId, request, user);
        return ResponseEntity.ok(CommonResponse.ok());
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<CommonResponse<Void>> delete(
            @PathVariable @Positive @NonNull Long challengeId,
            @PathVariable @Positive @NonNull Long answerId,
            @PathVariable @Positive @NonNull Long commentId,
            @CurrentUser @NonNull User user) {

        commandService.delete(commentId, user);
        return ResponseEntity.ok(CommonResponse.ok());
    }
}
