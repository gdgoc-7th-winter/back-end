package com.project.post.presentation.controller;

import com.project.global.annotation.CurrentUser;
import com.project.global.response.CommonResponse;
import com.project.post.application.dto.PostCreateResponse;
import com.project.post.application.dto.RecruitingPost.*;
import com.project.post.application.service.ApplicationFormQueryService;
import com.project.post.application.service.RecruitingApplicationCommandService;
import com.project.post.application.service.RecruitingPostCommandService;
import com.project.post.application.service.RecruitingPostQueryService;
import com.project.user.domain.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@Validated
@RequiredArgsConstructor
public class RecruitingPostController {

    private final RecruitingPostCommandService recruitingPostCommandService;
    private final RecruitingApplicationCommandService recruitingApplicationCommandService;
    private final RecruitingPostQueryService recruitingPostQueryService;
    private final ApplicationFormQueryService applicationFormQueryService;

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

    @PostMapping("/recruitings/applications")
    public CommonResponse<Void> submitApplication(
            @RequestBody @Valid SubmitApplicationRequest request,
            @CurrentUser User user
    ) {
        recruitingApplicationCommandService.submit(request, user);
        return CommonResponse.ok();
    }

    @GetMapping("/recruitings/{postId}")
    public CommonResponse<RecruitingPostDetailResponse> getDetail(@PathVariable Long postId) {
        return CommonResponse.ok(recruitingPostQueryService.getDetail(postId));
    }

    @GetMapping("/recruitings/{postId}/application-form")
    public CommonResponse<ApplicationFormDetailResponse> getApplicationFormDetail(
            @PathVariable Long postId
    ) {
        return CommonResponse.ok(applicationFormQueryService.getApplicationFormDetail(postId));
    }

    @PatchMapping("/{postId}")
    public ResponseEntity<CommonResponse<RecruitingPostDetailResponse>> update(
            @PathVariable Long postId,
            @Valid @RequestBody RecruitingPostUpdateRequest request,
            @CurrentUser User user
    ) {
        return ResponseEntity.ok(
                CommonResponse.ok(
                        recruitingPostCommandService.update(postId, request, user)
                )
        );
    }
}