package com.project.contribution.presentation.controller;

import com.project.contribution.application.service.ContributionScoreService;
import com.project.contribution.presentation.dto.ScoreCreateRequest;
import com.project.contribution.presentation.dto.ScoreResponse;
import com.project.contribution.presentation.dto.ScoreUpdateRequest;
import com.project.contribution.presentation.swagger.ContributionScoreControllerDocs;
import com.project.global.response.CommonResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1/scores")
@RequiredArgsConstructor
public class ContributionScoreController implements ContributionScoreControllerDocs {
    private final ContributionScoreService scoreService;

    @Override
    @PostMapping
    public ResponseEntity<CommonResponse<ScoreResponse>> addScore(@RequestBody ScoreCreateRequest request) {
        ScoreResponse score = ScoreResponse.from(scoreService.addScore(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(CommonResponse.ok(score));
    }

    @Override
    @PatchMapping("/{id}")
    public ResponseEntity<CommonResponse<ScoreResponse>> editScore(@PathVariable Long id, @RequestBody ScoreUpdateRequest request) {
        ScoreResponse score = ScoreResponse.from(scoreService.editScore(id, request));
        return ResponseEntity.ok(CommonResponse.ok(score));
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<ScoreResponse>> getScore(@PathVariable Long id) {
        ScoreResponse score = ScoreResponse.from(scoreService.getScore(id));
        return ResponseEntity.ok(CommonResponse.ok(score));
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponse<Void>> deleteScore(@PathVariable Long id) {
        scoreService.deleteScore(id);
        return ResponseEntity.ok(CommonResponse.ok());
    }
}
