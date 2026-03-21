package com.project.user.presentation.swagger;

import com.project.global.response.CommonResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import com.project.user.presentation.dto.request.EmailSendRequest;
import com.project.user.presentation.dto.request.EmailVerificationRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "UserAuth", description = "인증 API (이메일 인증)")
public interface UserAuthControllerDocs {

    @Operation(summary = "이메일 인증 발송", description = "이메일로 인증 코드를 발송합니다.")
    @ApiResponse(responseCode = "200", description = "성공")
    ResponseEntity<CommonResponse<Void>> sendEmail(
            @RequestBody(description = "이메일 발송 요청") EmailSendRequest request);

    @Operation(summary = "인증 코드 검증", description = "이메일과 인증 코드를 검증합니다.")
    @ApiResponse(responseCode = "200", description = "성공")
    ResponseEntity<CommonResponse<Void>> verifyCode(
            @RequestBody(description = "인증 코드 검증 요청") EmailVerificationRequest request);
}
