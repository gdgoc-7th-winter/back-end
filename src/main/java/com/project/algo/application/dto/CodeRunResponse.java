package com.project.algo.application.dto;

/**
 * @param stdout    표준 출력 결과
 * @param stderr    표준 에러 출력
 * @param status    실행 결과 상태 (ACCEPTED / RUNTIME_ERROR / KILLED)
 * @param exitCode  프로세스 종료 코드
 */
public record CodeRunResponse(
        String stdout,
        String stderr,
        String status,
        int exitCode
) {}
