package com.project.user.presentation.controller;

import com.project.user.presentation.swagger.UserAuthControllerDocs;
import com.project.global.response.CommonResponse;
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
public class UserAuthController implements UserAuthControllerDocs {
    private final EmailService emailService;

    @Override
    @PostMapping("/email-verification")
    public CommonResponse<Void> sendEmail(@RequestBody @Valid EmailSendRequest request) {
        emailService.sendAuthEmail(request.email());
        return CommonResponse.ok();
    }

    @Override
    @PostMapping("/verify-code")
    public CommonResponse<Void> verifyCode(@RequestBody @Valid EmailVerificationRequest request) {
        emailService.verifyCode(request.email(), request.authCode());
        return CommonResponse.ok();
    }
}
