package com.project.algo.application.service.impl;

import com.project.algo.application.dto.CodeRunRequest;
import com.project.algo.application.dto.CodeRunResponse;
import com.project.algo.application.service.CodeExecutionService;
import com.project.algo.domain.enums.ProgrammingLanguage;
import com.project.algo.infrastructure.piston.PistonRunRequest;
import com.project.algo.infrastructure.piston.PistonRunResponse;
import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CodeExecutionServiceImpl implements CodeExecutionService {

    private final RestClient pistonClient;

    @Value("${piston.api.key:}")
    private String pistonApiKey;

    @Override
    public CodeRunResponse run(CodeRunRequest request) {
        ProgrammingLanguage lang = request.language();

        PistonRunRequest pistonRequest = new PistonRunRequest(
                lang.getPistonLanguage(),
                "*",
                List.of(new PistonRunRequest.PistonFile(resolveFileName(lang), request.code())),
                request.stdin() != null ? request.stdin() : ""
        );

        PistonRunResponse response;
        try {
            RestClient.RequestBodySpec spec = pistonClient.post().body(pistonRequest);
            if (pistonApiKey != null && !pistonApiKey.isBlank()) {
                spec = spec.header("Authorization", pistonApiKey);
            }
            response = spec.retrieve().body(PistonRunResponse.class);
        } catch (RestClientResponseException e) {
            // Piston API가 4xx/5xx HTTP 응답을 반환한 경우
            log.error("Piston API HTTP 오류: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new BusinessException(ErrorCode.CODE_EXECUTION_ERROR,
                    "코드 실행 서비스 오류 (HTTP " + e.getStatusCode().value() + ")");
        } catch (ResourceAccessException e) {
            // 네트워크 연결 실패, DNS 오류, TLS 오류 등
            log.error("Piston API 연결 실패: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.CODE_EXECUTION_ERROR, "코드 실행 서비스에 연결할 수 없습니다.");
        }

        if (response == null || response.run() == null) {
            throw new BusinessException(ErrorCode.CODE_EXECUTION_ERROR, "코드 실행 결과를 받지 못했습니다.");
        }

        PistonRunResponse.RunResult run = response.run();
        return new CodeRunResponse(
                run.stdout(),
                run.stderr(),
                resolveStatus(run),
                run.code()
        );
    }

    /**
     * Java는 public 클래스명과 파일명이 일치해야 실행되므로 "Main.java"로 고정.
     * 나머지는 "main" + 확장자.
     */
    private String resolveFileName(ProgrammingLanguage lang) {
        if (lang == ProgrammingLanguage.JAVA) {
            return "Main.java";
        }
        return "main" + lang.getFileExtension();
    }

    private String resolveStatus(PistonRunResponse.RunResult run) {
        if (run.signal() != null) return "KILLED";
        if (run.code() != 0)     return "RUNTIME_ERROR";
        return "ACCEPTED";
    }

}
