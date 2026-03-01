package com.project.global.presentation.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.Map;

@Tag(name = "ApiHealth", description = "API 헬스 체크")
public interface ApiHealthControllerDocs {

    @Operation(summary = "헬스 체크", description = "서버 상태 및 타임스탬프를 확인합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공")
    Map<String, String> health();

    @Operation(summary = "핑", description = "서버가 응답 가능한지 확인합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공")
    String ping();
}
