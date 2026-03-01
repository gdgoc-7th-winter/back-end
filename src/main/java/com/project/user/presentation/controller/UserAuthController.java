package com.project.user.presentation.controller;
import com.project.global.response.ApiResponse;
import com.project.user.application.service.EmailService;
import com.project.user.presentation.dto.request.EmailSendRequest;
import com.project.user.presentation.dto.request.EmailVerificationRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserAuthController {
    private final EmailService emailService;

    @PostMapping("/email-verification")
    public ApiResponse<Void> sendEmail(@RequestBody @Valid EmailSendRequest request) {
        emailService.sendAuthEmail(request.email());
        return ApiResponse.ok();
    }

    @PostMapping("/verify-code")
    public ApiResponse<Void> verifyCode(@RequestBody EmailVerificationRequest request) {
        emailService.verifyCode(request.email(), request.authCode());
        return ApiResponse.ok();
    }
}
