package com.project.post.presentation.controller;

import com.project.global.annotation.CurrentUser;
import com.project.global.annotation.OptionalSessionUser;
import com.project.global.response.CommonResponse;
import com.project.global.response.PostPageResponse;
import com.project.post.application.dto.PostCreateResponse;
import com.project.post.application.dto.PromotionPost.PromotionPostCreateRequest;
import com.project.post.application.dto.PromotionPost.PromotionPostDetailResponse;
import com.project.post.application.dto.PromotionPost.PromotionPostListResponse;
import com.project.post.application.dto.PromotionPost.PromotionPostUpdateRequest;
import com.project.post.application.service.PromotionPostCommandService;
import com.project.post.application.service.PromotionPostQueryService;
import com.project.post.domain.enums.PromotionCategory;
import com.project.post.presentation.support.ViewerUserId;
import com.project.post.presentation.swagger.PromotionPostControllerDocs;
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

import java.util.Optional;

@RestController
@RequestMapping("/api/v1")
@Validated
@RequiredArgsConstructor
public class PromotionPostController implements PromotionPostControllerDocs {

    private final PromotionPostCommandService promotionPostCommandService;
    private final PromotionPostQueryService promotionPostQueryService;

    @Override
    @PostMapping("/promotions")
    public ResponseEntity<CommonResponse<PostCreateResponse>> create(
            @RequestBody @Valid @NonNull PromotionPostCreateRequest request,
            @CurrentUser @NonNull User user
    ) {
        Long postId = promotionPostCommandService.create(request, user);

        PostCreateResponse response = new PostCreateResponse(postId);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CommonResponse.ok(response));
    }

    @Override
    @GetMapping("/promotions/{postId}")
    public ResponseEntity<CommonResponse<PromotionPostDetailResponse>> getDetail(
            @PathVariable @Positive @NonNull Long postId,
            @OptionalSessionUser Optional<User> optionalViewer
    ) {
        PromotionPostDetailResponse response = promotionPostQueryService.getDetail(postId, ViewerUserId.from(optionalViewer));
        return ResponseEntity.ok(CommonResponse.ok(response));
    }

    @Override
    @GetMapping("/promotions")
    public ResponseEntity<CommonResponse<PostPageResponse<PromotionPostListResponse>>> getList(
            @RequestParam(required = false) PromotionCategory category,
            @ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) @NonNull Pageable pageable,
            @OptionalSessionUser Optional<User> optionalViewer
    ) {
        Page<PromotionPostListResponse> list = promotionPostQueryService.getList(category, pageable, ViewerUserId.from(optionalViewer));
        return ResponseEntity.ok(CommonResponse.ok(PostPageResponse.of(list)));
    }

    @Override
    @PatchMapping("/promotions/{postId}")
    public ResponseEntity<CommonResponse<Void>> update(
            @PathVariable @Positive @NonNull Long postId,
            @RequestBody @Valid @NonNull PromotionPostUpdateRequest request,
            @CurrentUser @NonNull User user
    ) {
        promotionPostCommandService.update(postId, request, user);
        return ResponseEntity.ok(CommonResponse.ok());
    }

    @Override
    @DeleteMapping("/promotions/{postId}")
    public ResponseEntity<CommonResponse<Void>> delete(
            @PathVariable @Positive @NonNull Long postId,
            @CurrentUser @NonNull User user
    ) {
        promotionPostCommandService.delete(postId, user);
        return ResponseEntity.ok(CommonResponse.ok());
    }
}
