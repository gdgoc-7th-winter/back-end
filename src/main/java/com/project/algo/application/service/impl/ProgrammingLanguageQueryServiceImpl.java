package com.project.algo.application.service.impl;

import com.project.algo.application.dto.ProgrammingLanguageResponse;
import com.project.algo.application.service.ProgrammingLanguageQueryService;
import com.project.algo.domain.enums.ProgrammingLanguage;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class ProgrammingLanguageQueryServiceImpl implements ProgrammingLanguageQueryService {

    @Override
    public List<ProgrammingLanguageResponse> getAll() {
        return Arrays.stream(ProgrammingLanguage.values())
                .map(ProgrammingLanguageResponse::from)
                .toList();
    }
}
