package com.project.file.presentation.swagger;

import com.project.file.application.dto.FileCompleteRequest;
import com.project.file.application.dto.FileCompleteResponse;
import com.project.file.application.dto.PresignedUrlRequest;
import com.project.file.application.dto.PresignedUrlResponse;
import com.project.global.response.CommonResponse;
import com.project.user.domain.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;

@Tag(name = "File", description = "파일 업로드 API (S3 Presigned URL)")
public interface FileControllerDocs {

    @Operation(
            summary = "Presigned URL 발급",
            description = """
                    S3 직접 업로드용 Presigned URL을 발급합니다.
                    1. 이 API로 uploadUrl, objectKey, expiresIn을 받습니다.
                    2. 클라이언트는 uploadUrl로 PUT 요청하여 파일을 업로드합니다.
                    3. 업로드 완료 후 POST /api/v1/files/complete를 호출하여 검증 및 DB 저장을 완료합니다.
                    - uploadType: PROFILE_IMAGE(프로필), POST_IMAGE(게시글), ATTACHMENT(첨부파일)
                    - contentType: image/jpeg, image/png, image/webp, image/gif, application/pdf 등
                    - referenceId: PROFILE_IMAGE는 사용자 ID, POST_IMAGE/ATTACHMENT는 게시글 ID
                    """
    )
    @ApiResponse(responseCode = "201", description = "Presigned URL 발급 성공")
    @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content(schema = @Schema(hidden = true)))
    ResponseEntity<CommonResponse<PresignedUrlResponse>> createPresignedUrl(
            @RequestBody(description = "Presigned URL 발급 요청") @Valid @NonNull PresignedUrlRequest request,
            @Parameter(hidden = true) @NonNull User user
    );

    @Operation(
            summary = "업로드 완료",
            description = """
                    S3 업로드 완료 후 검증 및 DB 저장을 요청합니다.
                    - objectKey: Presigned URL 발급 시 받은 objectKey
                    - uploadType, contentType, size: Presigned URL 발급 시와 동일한 값
                    - S3에 실제 파일 존재 여부, content-type, size 검증 후 FileMetadata 저장
                    """
    )
    @ApiResponse(responseCode = "201", description = "업로드 완료 처리 성공")
    @ApiResponse(responseCode = "401", description = "인증 필요", content = @Content(schema = @Schema(hidden = true)))
    ResponseEntity<CommonResponse<FileCompleteResponse>> completeUpload(
            @RequestBody(description = "업로드 완료 요청") @Valid @NonNull FileCompleteRequest request,
            @Parameter(hidden = true) @NonNull User user
    );
}
