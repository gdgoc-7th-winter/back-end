package com.project.algo.application.service;

import com.project.algo.application.dto.ProgrammingLanguageResponse;

import java.util.List;

public interface ProgrammingLanguageQueryService {

    List<ProgrammingLanguageResponse> getAll();
}
