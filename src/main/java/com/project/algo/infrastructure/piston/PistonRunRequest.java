package com.project.algo.infrastructure.piston;

import java.util.List;

public record PistonRunRequest(
        String language,
        String version,
        List<PistonFile> files,
        String stdin
) {
    public record PistonFile(String name, String content) {}
}
