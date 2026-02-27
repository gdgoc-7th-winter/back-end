package com.project.user.presentation.controller;

import com.project.user.application.service.EmailService;
import com.project.user.presentation.dto.EmailSendRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserAuthController {

    private final EmailService emailService;

    @PostMapping("/email/send")
    public ResponseEntity<String> sendEmail(@RequestBody EmailSendRequest request) {
        emailService.sendAuthEmail(request.email());
        return ResponseEntity.ok("메일이 발송되었습니다. 수신함을 확인하세요!");
    }
}
