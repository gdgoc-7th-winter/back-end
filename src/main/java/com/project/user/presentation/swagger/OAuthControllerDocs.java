package com.project.user.presentation.swagger;

import com.project.global.response.CommonResponse;
import com.project.user.application.dto.OAuthCodeRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;

@Tag(name = "OAuth2", description = "소셜 로그인 및 계정 연동 API")
public interface OAuthControllerDocs {

    @Operation(
            summary = "소셜 로그인",
            description = "프론트엔드에서 OAuth2 인가 코드를 발급받은 후 백엔드로 전달합니다. "
                    + "백엔드가 액세스 토큰 교환 및 사용자 정보 조회를 수행하고 세션을 생성합니다. "
                    + "지원 provider: google, kakao, naver, github"
    )
    @ApiResponse(responseCode = "200", description = "로그인 성공 (세션 생성됨)")
    @ApiResponse(responseCode = "400", description = "잘못된 provider 또는 인가 코드")
    @ApiResponse(responseCode = "401", description = "연동된 계정 없음")
    ResponseEntity<CommonResponse<Void>> loginWithProvider(
            @Parameter(description = "OAuth2 provider (google, kakao, naver, github)")
            @NonNull String provider,
            @RequestBody(description = "인가 코드 및 리다이렉트 URI") @Valid @NonNull OAuthCodeRequest request,
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse
    );

    @Operation(
            summary = "소셜 계정 연동",
            description = "로그인된 사용자의 계정에 소셜 로그인 수단을 추가합니다. "
                    + "프론트엔드에서 OAuth2 인가 코드를 발급받은 후 백엔드로 전달합니다. "
                    + "지원 provider: google, kakao, naver, github"
    )
    @ApiResponse(responseCode = "200", description = "연동 성공")
    @ApiResponse(responseCode = "404", description = "세션 만료 또는 사용자 없음")
    @ApiResponse(responseCode = "409", description = "이미 연동된 소셜 계정")
    ResponseEntity<CommonResponse<Void>> connectProvider(
            @Parameter(description = "OAuth2 provider (google, kakao, naver, github)")
            @NonNull String provider,
            @RequestBody(description = "인가 코드 및 리다이렉트 URI") @Valid @NonNull OAuthCodeRequest request
    );
}
