package com.project.post.presentation.swagger;

import com.project.global.annotation.OptionalSessionUser;
import com.project.global.response.CommonResponse;
import com.project.global.response.PageResponse;
import com.project.post.application.dto.LecturePost.LecturePostCreateRequest;
import com.project.post.application.dto.LecturePost.LecturePostDetailResponse;
import com.project.post.application.dto.LecturePost.LecturePostListResponse;
import com.project.post.application.dto.LecturePost.LecturePostUpdateRequest;
import com.project.post.application.dto.PostCreateResponse;
import com.project.post.domain.enums.Campus;
import com.project.user.domain.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;

@Tag(name = "Lecture Post", description = "강의/수업 게시글 API")
public interface LecturePostControllerDocs {

    @Operation(
            summary = "강의/수업 게시글 목록 조회",
            description = """
                    강의/수업 게시판의 게시글 목록을 페이징하여 조회합니다.
                    캠퍼스(서울/글로벌) 필터, 학과 다중 필터, 태그 다중 필터, 키워드 통합 검색을 지원합니다.
                    키워드 검색은 제목, 본문, 학과, 캠퍼스, 태그를 대상으로 합니다.
                    """
    )
    @ApiResponse(responseCode = "200", description = "성공")
    ResponseEntity<CommonResponse<PageResponse<LecturePostListResponse>>> getList(
            @Parameter(description = "검색 키워드 (제목/본문/학과/캠퍼스/태그 부분 일치)") String keyword,
            @Parameter(description = "태그 필터 (복수 가능)") List<String> tags,
            @Parameter(description = "캠퍼스 필터 (SEOUL: 서울, GLOBAL: 글로벌)") Campus campus,
            @Parameter(description = "학과 필터 (복수 가능)") List<String> departments,
            @Parameter(description = "정렬 기준 (latest: 최신, views: 조회수, likes: 좋아요)") String order,
            @NonNull Pageable pageable,
            @Parameter(hidden = true) @OptionalSessionUser Optional<User> optionalViewer
    );

    @Operation(summary = "강의/수업 게시글 상세 조회", description = "게시글 ID로 강의/수업 게시글 상세 정보를 조회합니다. 로그인 시 viewer.liked / viewer.scrapped / viewer.isAuthor 에 현재 사용자 기준 상태가 포함됩니다.")
    @ApiResponse(responseCode = "200", description = "성공")
    ResponseEntity<CommonResponse<LecturePostDetailResponse>> getDetail(
            @Parameter(description = "게시글 ID") @Positive @NonNull Long postId,
            @Parameter(hidden = true) @OptionalSessionUser Optional<User> optionalViewer
    );

    @Operation(summary = "강의/수업 게시글 작성", description = "새 강의/수업 게시글을 작성합니다. 학과와 캠퍼스는 필수 입력입니다.")
    @ApiResponse(responseCode = "201", description = "생성됨")
    @ApiResponse(responseCode = "401", description = "인증 필요")
    ResponseEntity<CommonResponse<PostCreateResponse>> create(
            @RequestBody(description = "강의/수업 게시글 작성 요청") @Valid @NonNull LecturePostCreateRequest request,
            @Parameter(hidden = true) @NonNull User user
    );

    @Operation(summary = "강의/수업 게시글 수정", description = "강의/수업 게시글을 수정합니다.")
    @ApiResponse(responseCode = "200", description = "성공")
    @ApiResponse(responseCode = "401", description = "인증 필요")
    ResponseEntity<CommonResponse<Void>> update(
            @Parameter(description = "게시글 ID") @Positive @NonNull Long postId,
            @RequestBody(description = "강의/수업 게시글 수정 요청") @Valid @NonNull LecturePostUpdateRequest request,
            @Parameter(hidden = true) @NonNull User user
    );

    @Operation(summary = "강의/수업 게시글 삭제", description = "강의/수업 게시글을 소프트 삭제합니다.")
    @ApiResponse(responseCode = "200", description = "성공")
    @ApiResponse(responseCode = "401", description = "인증 필요")
    ResponseEntity<CommonResponse<Void>> delete(
            @Parameter(description = "게시글 ID") @Positive @NonNull Long postId,
            @Parameter(hidden = true) @NonNull User user
    );
}
