package com.project.algo.application.service;

import com.project.algo.application.dto.CodeRunRequest;
import com.project.algo.application.dto.CodeRunResponse;

public interface CodeExecutionService {

    CodeRunResponse run(CodeRunRequest request);
}
