package com.project.user.presentation.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "OAuth2", description = "소셜 로그인 및 계정 연동 API")
public interface OAuthControllerDocs {

    @Operation(
            summary = "소셜 로그인",
            description = "지정한 provider의 OAuth2 인가 서버로 리다이렉트합니다. "
                    + "비로그인 상태에서 사용합니다. "
                    + "지원 provider: google, kakao, naver, github"
    )
    @ApiResponse(responseCode = "302", description = "OAuth2 인가 서버로 리다이렉트")
    String loginWithProvider(
            @Parameter(description = "OAuth2 provider (google, kakao, naver, github)")
            @PathVariable("provider") String provider);

    @Operation(
            summary = "소셜 계정 연동",
            description = "로그인된 사용자의 계정에 소셜 로그인 수단을 추가합니다. "
                    + "로그인 상태에서만 사용 가능합니다. "
                    + "지원 provider: google, kakao, naver, github"
    )
    @ApiResponse(responseCode = "302", description = "OAuth2 인가 서버로 리다이렉트")
    @ApiResponse(responseCode = "401", description = "세션 만료")
    String connectProvider(
            @Parameter(description = "OAuth2 provider (google, kakao, naver, github)")
            @PathVariable("provider") String provider);
}
