package com.project.algo.presentation.controller;

import com.project.algo.application.dto.DailyChallengeCreateRequest;
import com.project.algo.application.dto.DailyChallengeDetailResponse;
import com.project.algo.application.dto.DailyChallengeListResponse;
import com.project.algo.application.dto.DailyChallengeUpdateRequest;
import com.project.algo.domain.enums.AlgorithmTag;
import com.project.algo.application.service.DailyChallengeCommandService;
import com.project.algo.application.service.DailyChallengeQueryService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/algo")
@Validated
@RequiredArgsConstructor
public class DailyChallengeController {

    private final DailyChallengeCommandService commandService;
    private final DailyChallengeQueryService queryService;

    @GetMapping
    public ResponseEntity<CommonResponse<PageResponse<DailyChallengeListResponse>>> getList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, name = "tags") List<AlgorithmTag> algorithmTags,
            @ParameterObject
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            @NonNull Pageable pageable) {

        Page<DailyChallengeListResponse> page = queryService.getList(keyword, algorithmTags, pageable);
        return ResponseEntity.ok(CommonResponse.ok(PageResponse.of(page)));
    }

    @GetMapping("/{challengeId}")
    public ResponseEntity<CommonResponse<DailyChallengeDetailResponse>> getDetail(
            @PathVariable @Positive @NonNull Long challengeId) {

        return ResponseEntity.ok(CommonResponse.ok(queryService.getDetail(challengeId)));
    }

    @PostMapping
    public ResponseEntity<CommonResponse<Map<String, Long>>> create(
            @RequestBody @Valid @NonNull DailyChallengeCreateRequest request,
            @CurrentUser @NonNull User user) {

        Long challengeId = commandService.create(request, user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonResponse.ok(Map.of("challengeId", challengeId)));
    }

    @PatchMapping("/{challengeId}")
    public ResponseEntity<CommonResponse<Void>> update(
            @PathVariable @Positive @NonNull Long challengeId,
            @RequestBody @Valid @NonNull DailyChallengeUpdateRequest request,
            @CurrentUser @NonNull User user) {

        commandService.update(challengeId, request, user);
        return ResponseEntity.ok(CommonResponse.ok());
    }

    @DeleteMapping("/{challengeId}")
    public ResponseEntity<CommonResponse<Void>> delete(
            @PathVariable @Positive @NonNull Long challengeId,
            @CurrentUser @NonNull User user) {

        commandService.delete(challengeId, user);
        return ResponseEntity.ok(CommonResponse.ok());
    }
}
