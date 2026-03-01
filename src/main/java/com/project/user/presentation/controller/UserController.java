package com.project.user.presentation.controller;

import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.global.response.ApiResponse;

import com.project.user.application.dto.UserSession;
import com.project.user.application.dto.response.ProfileResponse;
import com.project.user.application.service.UserService;

import com.project.user.presentation.dto.request.LoginRequest;
import com.project.user.presentation.dto.request.ProfileUpdateRequest;
import com.project.user.presentation.dto.request.SignUpRequest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<String>> signUp(@RequestBody @Valid SignUpRequest request) {
        userService.signUp(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("회원가입이 완료되었습니다."));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> login(
            @RequestBody @Valid LoginRequest loginRequest,
            HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        userService.login(loginRequest,session,request);

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(ApiResponse.ok("정상 로그인처리 되었습니다."));
    }

    @PostMapping("/profile-setup")
    public ResponseEntity<?> setupProfile(
            @RequestBody @Valid ProfileUpdateRequest request,
            HttpSession session
    ) {
        UserSession user = (UserSession) session.getAttribute("LOGIN_USER");

        if (user == null) {
            throw new BusinessException(ErrorCode.SESSION_NOT_FOUND, "세션이 만료되었습니다.");
        }

        userService.completeInitialProfile(user.getEmail(), request, session);

        return ResponseEntity.ok(ApiResponse.ok("프로필 설정이 완료되었습니다."));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<ProfileResponse>> getMyProfile(HttpSession session) {
        UserSession sessionUser = (UserSession) session.getAttribute("LOGIN_USER");

        if (sessionUser == null) {
            throw new BusinessException(ErrorCode.SESSION_NOT_FOUND);
        }

        ProfileResponse response = userService.getUserProfile(sessionUser.getEmail());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
