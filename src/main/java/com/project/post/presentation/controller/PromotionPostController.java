package com.project.post.presentation.controller;

import com.project.global.annotation.CurrentUser;
import com.project.global.response.CommonResponse;
import com.project.post.application.dto.PromotionPost.*;
import com.project.post.application.service.PromotionPostCommandService;
import com.project.post.application.service.PromotionPostQueryService;
import com.project.post.domain.enums.PromotionCategory;
import com.project.user.domain.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@Validated
@RequiredArgsConstructor
public class PromotionPostController {

    private final PromotionPostCommandService promotionPostCommandService;
    private final PromotionPostQueryService promotionPostQueryService;

    @PostMapping("/promotions")
    public ResponseEntity<CommonResponse<PromotionPostCreateResponse>> create(
            @RequestBody @Valid @NonNull PromotionPostCreateRequest request,
            @CurrentUser @NonNull User user
    ) {
        Long postId = promotionPostCommandService.create(request, user);

        PromotionPostCreateResponse response = new PromotionPostCreateResponse(postId, "게시글이 성공적으로 등록되었습니다.");
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CommonResponse.ok(response));
    }

    @GetMapping("/promotions/{postId}")
    public ResponseEntity<CommonResponse<PromotionPostDetailResponse>> getDetail(
            @PathVariable @NonNull Long postId
    ) {
        PromotionPostDetailResponse response = promotionPostQueryService.getDetail(postId);
        return ResponseEntity.ok(CommonResponse.ok(response));
    }

    @GetMapping("/promotions")
    public ResponseEntity<CommonResponse<Page<PromotionPostListResponse>>> getList(
            @RequestParam(required = false) PromotionCategory category,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<PromotionPostListResponse> list = promotionPostQueryService.getList(category, pageable);
        return ResponseEntity.ok(CommonResponse.ok(list));
    }

    @PatchMapping("/promotions/{id}")
    public ResponseEntity<CommonResponse<Void>> update(
            @PathVariable Long id,
            @RequestBody @Valid @NonNull PromotionPostUpdateRequest request,
            @CurrentUser @NonNull User user
    ) {
        promotionPostCommandService.update(id, request, user);
        return ResponseEntity.ok(CommonResponse.ok());
    }
}