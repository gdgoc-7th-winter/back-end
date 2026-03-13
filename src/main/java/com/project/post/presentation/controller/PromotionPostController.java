package com.project.post.presentation.controller;

import com.project.global.annotation.CurrentUser;
import com.project.global.response.CommonResponse;

import com.project.post.application.dto.PromotionPost.PromotionPostCreateRequest;
import com.project.post.application.dto.PromotionPost.PromotionPostCreateResponse;
import com.project.post.application.dto.PromotionPost.PromotionPostDetailResponse;
import com.project.post.application.dto.PromotionPost.PromotionPostListResponse;
import com.project.post.application.dto.PromotionPost.PromotionPostUpdateRequest;
import com.project.post.application.service.PromotionPostCommandService;
import com.project.post.application.service.PromotionPostQueryService;
import com.project.post.domain.enums.PromotionCategory;
import com.project.post.presentation.swagger.PromotionPostControllerDocs;
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

import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.DeleteMapping;

@RestController
@RequestMapping("/api/v1")
@Validated
@RequiredArgsConstructor
public class PromotionPostController implements PromotionPostControllerDocs {

    private final PromotionPostCommandService promotionPostCommandService;
    private final PromotionPostQueryService promotionPostQueryService;

    @Override
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

    @Override
    @GetMapping("/promotions/{postId}")
    public ResponseEntity<CommonResponse<PromotionPostDetailResponse>> getDetail(
            @PathVariable @Positive @NonNull Long postId
    ) {
        PromotionPostDetailResponse response = promotionPostQueryService.getDetail(postId);
        return ResponseEntity.ok(CommonResponse.ok(response));
    }

    @Override
    @GetMapping("/promotions")
    public ResponseEntity<CommonResponse<Page<PromotionPostListResponse>>> getList(
            @RequestParam(required = false) PromotionCategory category,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<PromotionPostListResponse> list = promotionPostQueryService.getList(category, pageable);
        return ResponseEntity.ok(CommonResponse.ok(list));
    }

    @Override
    @PatchMapping("/promotions/{id}")
    public ResponseEntity<CommonResponse<Void>> update(
            @PathVariable @Positive @NonNull Long id,
            @RequestBody @Valid @NonNull PromotionPostUpdateRequest request,
            @CurrentUser @NonNull User user
    ) {
        promotionPostCommandService.update(id, request, user);
        return ResponseEntity.ok(CommonResponse.ok());
    }

    @Override
    @DeleteMapping("/promotions/{id}")
    public ResponseEntity<CommonResponse<Void>> delete(
            @PathVariable @Positive @NonNull Long id,
            @CurrentUser @NonNull User user
    ) {
        promotionPostCommandService.delete(id, user);
        return ResponseEntity.ok(CommonResponse.ok());
    }
}
