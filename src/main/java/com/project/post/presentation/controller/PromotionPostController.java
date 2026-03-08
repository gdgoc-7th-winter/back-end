package com.project.post.presentation.controller;

import com.project.global.annotation.CurrentUser;
import com.project.global.response.CommonResponse;
import com.project.post.application.dto.PromotionPostCreateRequest;
import com.project.post.application.dto.PromotionPostCreateResponse;
import com.project.post.application.service.PromotionPostCommandService;
import com.project.user.domain.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

    @PostMapping("/promotions")
    public ResponseEntity<PromotionPostCreateResponse> create(
            @RequestBody @Valid @NonNull PromotionPostCreateRequest request,
            @CurrentUser @NonNull User user
    ) {
        Long postId = promotionPostCommandService.create(request, user);

        PromotionPostCreateResponse response = new PromotionPostCreateResponse(postId, "게시글이 성공적으로 등록되었습니다.");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}