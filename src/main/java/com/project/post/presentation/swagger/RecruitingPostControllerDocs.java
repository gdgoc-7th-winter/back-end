package com.project.post.presentation.swagger;

import com.project.global.response.CommonResponse;
import com.project.post.application.dto.PostCreateResponse;
import com.project.post.application.dto.RecruitingPost.*;
import com.project.post.domain.enums.Campus;
import com.project.post.domain.enums.RecruitingCategory;
import com.project.user.domain.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Recruiting Post", description = "팀원 모집 게시글 API")
public interface RecruitingPostControllerDocs {

    @Operation(summary = "모집글 생성", description = "새로운 팀원 모집 게시글을 생성합니다.")
    @ApiResponse(responseCode = "201", description = "생성 성공")
    ResponseEntity<CommonResponse<PostCreateResponse>> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "모집글 생성 요청")
            @RequestBody @Valid RecruitingPostCreateRequest request,
            @Parameter(hidden = true) User user
    );

    @Operation(summary = "지원서 제출", description = "모집글에 지원합니다.")
    @ApiResponse(responseCode = "201", description = "생성 성공")
    ResponseEntity<CommonResponse<ApplicationSubmissionCreateResponse>> submitApplication(
            @Parameter(description = "게시글 ID") @PathVariable Long postId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "지원서 제출 요청")
            @RequestBody @Valid SubmitApplicationRequest request,
            @Parameter(hidden = true) User user
    );

    @Operation(summary = "모집글 상세 조회", description = "게시글 ID로 모집글 상세 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "성공")
    CommonResponse<RecruitingPostDetailResponse> getDetail(
            @Parameter(description = "게시글 ID") @PathVariable Long postId,
            @Parameter(hidden = true) User user
    );

    @Operation(summary = "지원폼 상세 조회", description = "특정 모집글의 지원폼 상세 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "성공")
    CommonResponse<ApplicationFormDetailResponse> getApplicationFormDetail(
            @Parameter(description = "게시글 ID") @PathVariable Long postId
    );

    @Operation(summary = "모집글 수정", description = "모집글을 수정합니다.")
    @ApiResponse(responseCode = "200", description = "성공")
    ResponseEntity<CommonResponse<RecruitingPostDetailResponse>> update(
            @Parameter(description = "게시글 ID") @PathVariable Long postId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "모집글 수정 요청")
            @RequestBody @Valid RecruitingPostUpdateRequest request,
            @Parameter(hidden = true) User user
    );

    @Operation(summary = "지원서 수정", description = "본인이 제출한 지원서를 수정합니다.")
    @ApiResponse(responseCode = "200", description = "성공")
    CommonResponse<Void> updateApplicationSubmission(
            @Parameter(description = "지원서 ID") @PathVariable Long submissionId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "지원서 수정 요청")
            @RequestBody @Valid ApplicationSubmissionUpdateRequest request,
            @Parameter(hidden = true) User user
    );

    @Operation(summary = "지원서 상세 조회", description = "지원서 상세 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "성공")
    CommonResponse<ApplicationSubmissionDetailResponse> getApplicationSubmissionDetail(
            @Parameter(description = "지원서 ID") @PathVariable Long submissionId,
            @Parameter(hidden = true) User user
    );

    CommonResponse<Page<ApplicationSubmissionSummaryResponse>> getApplicationSubmissionList(
            @PathVariable Long postId,
            @RequestParam(required = false) Campus campus,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) String applicantName,
            @RequestParam(defaultValue = "latest") String sort,
            @Parameter(hidden = true) Pageable pageable,
            @Parameter(hidden = true) User user
    );

    @Operation(summary = "지원서 취소", description = "본인이 제출한 지원서를 취소합니다.")
    @ApiResponse(responseCode = "200", description = "성공")
    CommonResponse<Void> cancelApplicationSubmission(
            @Parameter(description = "지원서 ID") @PathVariable Long submissionId,
            @Parameter(hidden = true) User user
    );

    @Operation(summary = "모집글 삭제", description = "모집글을 삭제합니다.")
    @ApiResponse(responseCode = "200", description = "성공")
    CommonResponse<Void> deleteRecruitingPost(
            @Parameter(description = "게시글 ID") @PathVariable Long postId,
            @Parameter(hidden = true) User user
    );

    @Operation(summary = "내가 쓴 모집글 목록 조회", description = "현재 로그인한 사용자가 작성한 모집글 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "성공")
    CommonResponse<MyRecruitingPostListResponse> getMyRecruitingPosts(
            @Parameter(hidden = true) User user
    );

    @Operation(summary = "내가 지원한 모집글 목록 조회", description = "현재 로그인한 사용자가 지원한 모집글 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "성공")
    CommonResponse<AppliedRecruitingPostListResponse> getAppliedRecruitings(
            @Parameter(hidden = true) User user
    );

    @Operation(summary = "모집글 목록 조회", description = "카테고리별 모집글 목록을 페이징 조회합니다.")
    @ApiResponse(responseCode = "200", description = "성공")
    CommonResponse<Page<RecruitingPostListResponse>> getRecruitingPostList(
            @Parameter(description = "카테고리 필터") @RequestParam(required = false) RecruitingCategory category,
            @Parameter(hidden = true) Pageable pageable,
            @Parameter(hidden = true) User user
    );
}
