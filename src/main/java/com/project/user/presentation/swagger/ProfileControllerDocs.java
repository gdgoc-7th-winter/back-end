package com.project.user.presentation.swagger;

import com.project.global.response.CommonResponse;
import com.project.user.application.dto.response.ProfileResponse;
import com.project.user.presentation.dto.request.ProfilePatchRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;

@Tag(name = "Profile", description = "내 프로필 API")
public interface ProfileControllerDocs {

    @Operation(summary = "내 프로필 조회", description = "로그인한 사용자의 프로필을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "성공")
    @ApiResponse(responseCode = "401", description = "세션 만료", content = @Content(schema = @Schema(hidden = true)))
    ResponseEntity<CommonResponse<ProfileResponse>> getMyProfile(
            @Parameter(hidden = true) HttpSession session);

    @Operation(summary = "프로필 수정", description = "로그인한 사용자의 프로필을 부분 수정합니다. 초기 설정 완료(USER 권한) 후 사용 가능합니다.")
    @ApiResponse(responseCode = "200", description = "성공")
    @ApiResponse(responseCode = "401", description = "세션 만료", content = @Content(schema = @Schema(hidden = true)))
    ResponseEntity<CommonResponse<String>> patchProfile(
            @RequestBody(description = "프로필 수정 요청") ProfilePatchRequest request,
            @Parameter(hidden = true) HttpSession session);

    @Operation(summary = "회원 탈퇴", description = "로그인한 사용자의 계정을 삭제합니다.")
    @ApiResponse(responseCode = "200", description = "성공")
    @ApiResponse(responseCode = "401", description = "세션 만료", content = @Content(schema = @Schema(hidden = true)))
    ResponseEntity<CommonResponse<String>> deleteAccount(
            @Parameter(hidden = true) HttpSession session);
}
