package com.project.contribution.presentation.controller;

import com.project.contribution.domain.entity.ContributionScore;
import com.project.contribution.application.service.ContributionScoreService;
import com.project.contribution.presentation.dto.ScoreCreateRequest;
import com.project.contribution.presentation.dto.ScoreUpdateRequest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1/scores")
@RequiredArgsConstructor
public class ContributionScoreController {

    private final ContributionScoreService scoreService;

    @PostMapping("/scoreCreate")
    public ResponseEntity<ContributionScore> addScore(Long id, ScoreCreateRequest request) {
        ContributionScore score = scoreService.addScore(id,request);
        return new ResponseEntity<>(score, HttpStatus.CREATED);
    }

    @PatchMapping("/scoreEdit/{id}")
    public ResponseEntity<ContributionScore> editScore(@PathVariable Long id, @RequestBody ScoreUpdateRequest request) {
        ContributionScore score = scoreService.editScore(id, request);
        return new ResponseEntity<>(score, HttpStatus.CREATED);
    }

    @GetMapping("/getscore/{id}")
    public ResponseEntity<ContributionScore> getScore(@PathVariable Long id) {
        ContributionScore score = scoreService.getScore(id);
        return new ResponseEntity<>(score, HttpStatus.OK);
    }

    @DeleteMapping("/deletescore/{id}")
    public void deleteScore(@PathVariable Long id) {
        scoreService.deleteScore(id);
    }
}
