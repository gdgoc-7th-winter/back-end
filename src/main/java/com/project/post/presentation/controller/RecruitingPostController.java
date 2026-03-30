package com.project.post.presentation.controller;

import com.project.global.annotation.CurrentUser;
import com.project.global.response.CommonResponse;
import com.project.post.application.dto.PostCreateResponse;
import com.project.post.application.dto.RecruitingPost.*;
import com.project.post.application.service.*;
import com.project.post.domain.enums.Campus;
import com.project.post.domain.enums.RecruitingCategory;
import com.project.user.domain.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final ApplicationSubmissionQueryService applicationSubmissionQueryService;

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

    @PostMapping("/recruitings/{postId}/applications")
    public ResponseEntity<CommonResponse<ApplicationSubmissionCreateResponse>> submitApplication(
            @PathVariable Long postId,
            @RequestBody @Valid SubmitApplicationRequest request,
            @CurrentUser User user
    ) {
        Long submissionId = recruitingApplicationCommandService.submit(postId, request, user);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CommonResponse.ok(new ApplicationSubmissionCreateResponse(submissionId)));
    }

    @GetMapping("/recruitings/{postId}")
    public CommonResponse<RecruitingPostDetailResponse> getDetail(@PathVariable Long postId, @CurrentUser User user) {
        return CommonResponse.ok(recruitingPostQueryService.getDetail(postId, user.getId()));
    }

    @GetMapping("/recruitings/{postId}/application-form")
    public CommonResponse<ApplicationFormDetailResponse> getApplicationFormDetail(
            @PathVariable Long postId
    ) {
        return CommonResponse.ok(applicationFormQueryService.getApplicationFormDetail(postId));
    }

    @PatchMapping("/recruitings/{postId}")
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

    @PatchMapping("/recruitings/applications/{submissionId}")
    public CommonResponse<Void> updateApplicationSubmission(
            @PathVariable Long submissionId,
            @RequestBody @Valid ApplicationSubmissionUpdateRequest request,
            @CurrentUser User user
    ) {
        recruitingApplicationCommandService.updateSubmission(submissionId, request, user);
        return CommonResponse.ok();
    }

    @GetMapping("/recruitings/applications/{submissionId}")
    public CommonResponse<ApplicationSubmissionDetailResponse> getApplicationSubmissionDetail(
            @PathVariable Long submissionId,
            @CurrentUser User user
    ) {
        return CommonResponse.ok(
                applicationSubmissionQueryService.getDetail(submissionId, user)
        );
    }

    @GetMapping("/recruitings/{postId}/applications")
    public CommonResponse<ApplicationSubmissionListResponse> getApplicationSubmissionList(
            @PathVariable Long postId,
            @RequestParam(required = false) Campus campus,
            @RequestParam(required = false) Long departmentId,
            @CurrentUser User user
    ) {
        return CommonResponse.ok(
                applicationSubmissionQueryService.getSubmissionList(postId, user, campus, departmentId)
        );
    }

    @DeleteMapping("/recruitings/applications/{submissionId}")
    public CommonResponse<Void> cancelApplicationSubmission(
            @PathVariable Long submissionId,
            @CurrentUser User user
    ) {
        recruitingApplicationCommandService.cancelSubmission(submissionId, user);
        return CommonResponse.ok();
    }

    @DeleteMapping("/recruitings/{postId}")
    public CommonResponse<Void> deleteRecruitingPost(
            @PathVariable Long postId,
            @CurrentUser User user
    ) {
        recruitingPostCommandService.delete(postId, user);
        return CommonResponse.ok();
    }

    @GetMapping("/recruitings/me")
    public CommonResponse<MyRecruitingPostListResponse> getMyRecruitingPosts(
            @CurrentUser User user
    ) {
        return CommonResponse.ok(
                recruitingPostQueryService.getMyRecruitingPosts(user.getId())
        );
    }

    @GetMapping("/recruitings/applied")
    public CommonResponse<AppliedRecruitingPostListResponse> getAppliedRecruitings(
            @CurrentUser User user
    ) {
        return CommonResponse.ok(
                applicationSubmissionQueryService.getAppliedRecruitings(user)
        );
    }

    @GetMapping("/recruitings")
    public CommonResponse<Page<RecruitingPostListResponse>> getRecruitingPostList(
            @RequestParam(required = false) RecruitingCategory category,
            Pageable pageable,
            @CurrentUser User user
    ) {
        return CommonResponse.ok(
                recruitingPostQueryService.getList(category, pageable, user.getId())
        );
    }
}