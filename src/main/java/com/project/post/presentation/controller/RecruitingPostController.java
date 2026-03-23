package com.project.post.presentation.controller;

import com.project.global.annotation.CurrentUser;
import com.project.global.response.CommonResponse;
import com.project.post.application.dto.PostCreateResponse;
import com.project.post.application.dto.RecruitingPost.RecruitingPostCreateRequest;
import com.project.post.application.service.RecruitingPostCommandService;
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
public class RecruitingPostController {

    private final RecruitingPostCommandService recruitingPostCommandService;

    @PostMapping("/recruitings")
    public ResponseEntity<CommonResponse<PostCreateResponse>> create(
            @RequestBody @Valid RecruitingPostCreateRequest request,
            @CurrentUser User user
    ) {
        Long postId = recruitingPostCommandService.create(request, user);

        PostCreateResponse response = new PostCreateResponse(postId);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CommonResponse.ok(response));
    }
}