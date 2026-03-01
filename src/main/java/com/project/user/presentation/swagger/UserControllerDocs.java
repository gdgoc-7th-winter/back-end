package com.project.user.presentation.swagger;

import com.project.global.response.CommonResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import com.project.user.application.dto.response.ProfileResponse;
import com.project.user.presentation.dto.request.LoginRequest;
import com.project.user.presentation.dto.request.ProfileUpdateRequest;
import com.project.user.presentation.dto.request.SignUpRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;

@Tag(name = "User", description = "사용자 API")
public interface UserControllerDocs {

    @Operation(summary = "회원가입", description = "새 사용자를 등록합니다.")
    @ApiResponse(responseCode = "201", description = "생성됨")
    ResponseEntity<CommonResponse<String>> signUp(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "회원가입 요청") SignUpRequest request);

    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인합니다.")
    @ApiResponse(responseCode = "202", description = "정상 처리됨")
    ResponseEntity<CommonResponse<String>> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "로그인 요청") LoginRequest loginRequest,
            HttpServletRequest request);

    @Operation(summary = "프로필 설정", description = "초기 프로필 설정을 완료합니다.")
    @ApiResponse(responseCode = "200", description = "성공")
    @ApiResponse(responseCode = "401", description = "세션 만료", content = @Content(schema = @Schema(hidden = true)))
    ResponseEntity<?> setupProfile(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "프로필 설정 요청") ProfileUpdateRequest request,
            HttpSession session);

    @Operation(summary = "내 프로필 조회", description = "로그인한 사용자의 프로필을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "성공")
    @ApiResponse(responseCode = "401", description = "세션 만료", content = @Content(schema = @Schema(hidden = true)))
    ResponseEntity<CommonResponse<ProfileResponse>> getMyProfile(HttpSession session);
}
