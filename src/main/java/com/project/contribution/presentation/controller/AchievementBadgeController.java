package com.project.contribution.presentation.controller;

import com.project.contribution.domain.entity.ContributionBadge;
import com.project.contribution.application.service.ContributionBadgeService;
import com.project.contribution.presentation.dto.BadgeCreateRequest;
import com.project.contribution.presentation.dto.BadgeUpdateRequest;

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
@RequestMapping("/api/v1/badges")
@RequiredArgsConstructor
public class AchievementBadgeController {

    private final ContributionBadgeService badgeService;

    @PostMapping("/badgeCreate")
    public ResponseEntity<ContributionBadge> addBadge(Long id, BadgeCreateRequest request) {
        ContributionBadge badge = badgeService.addBadge(id,request);
        return new ResponseEntity<>(badge, HttpStatus.CREATED);
    }

    @PatchMapping("/badgeEdit/{id}")
    public ResponseEntity<ContributionBadge> editBadge(@PathVariable Long id, @RequestBody BadgeUpdateRequest request) {
        ContributionBadge badge = badgeService.editBadge(id, request);
        return new ResponseEntity<>(badge, HttpStatus.CREATED);
    }

    @GetMapping("/getBadge/{id}")
    public ResponseEntity<ContributionBadge> getBadge(@PathVariable Long id) {
        ContributionBadge badge = badgeService.getBadge(id);
        return new ResponseEntity<>(badge, HttpStatus.OK);
    }

    @DeleteMapping("/deleteBadge/{id}")
    public void deleteBadge(@PathVariable Long id) {
        badgeService.deleteBadge(id);
    }
}
