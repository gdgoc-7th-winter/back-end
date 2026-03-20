package com.project.user.presentation.controller;

import com.project.user.application.service.EmailService;
import com.project.user.presentation.swagger.UserAuthControllerDocs;
import com.project.global.response.CommonResponse;
import com.project.user.presentation.dto.request.EmailSendRequest;
import com.project.user.presentation.dto.request.EmailVerificationRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class UserAuthController implements UserAuthControllerDocs {
    private final EmailService emailServiceImpl;

    @Override
    @PostMapping("/email-verification")
    public ResponseEntity<CommonResponse<Void>> sendEmail(@RequestBody @Valid EmailSendRequest request) {
        emailServiceImpl.sendAuthEmail(request.email());
        return ResponseEntity.ok(CommonResponse.ok());
    }

    @Override
    @PostMapping("/verify-code")
    public ResponseEntity<CommonResponse<Void>> verifyCode(@RequestBody @Valid EmailVerificationRequest request) {
        emailServiceImpl.verifyCode(request.email(), request.authCode());
        return ResponseEntity.ok(CommonResponse.ok());
    }
}
