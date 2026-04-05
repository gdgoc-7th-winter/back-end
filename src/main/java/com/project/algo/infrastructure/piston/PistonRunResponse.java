package com.project.algo.infrastructure.piston;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PistonRunResponse(
        String language,
        String version,
        RunResult run
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RunResult(
            String stdout,
            String stderr,
            int code,
            String signal,
            String output
    ) {}
}
