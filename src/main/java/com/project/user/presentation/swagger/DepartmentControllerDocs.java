package com.project.user.presentation.swagger;

import com.project.global.response.CommonResponse;
import com.project.user.application.dto.response.DepartmentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Tag(name = "Department", description = "학과 조회 API")
public interface DepartmentControllerDocs {

    @Operation(
            summary = "학과 목록 조회",
            description = "단과대 또는 학과명으로 검색합니다. 파라미터 없이 호출 시 전체 목록을 반환합니다."
                    + " college와 name을 동시에 입력하면 college 기준으로만 검색합니다."
    )
    @ApiResponse(responseCode = "200", description = "성공")
    ResponseEntity<CommonResponse<List<DepartmentResponse>>> getDepartments(
            @Parameter(description = "단과대 이름 (예: 공과대학)") String college,
            @Parameter(description = "학과명 검색어 (예: 컴퓨터)") String name
    );
}
