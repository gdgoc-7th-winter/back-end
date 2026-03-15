package com.project.user.presentation.controller;

import com.project.user.application.service.UserService;
import com.project.user.presentation.swagger.UserControllerDocs;
import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.global.response.CommonResponse;

import com.project.user.application.dto.UserSession;
import com.project.user.application.dto.response.ProfileResponse;

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
public class UserController implements UserControllerDocs {

    private final UserService userService;

    @Override
    @PostMapping("/signup")
    public ResponseEntity<CommonResponse<String>> signUp(@RequestBody @Valid SignUpRequest request) {
        userService.signUp(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonResponse.ok("회원가입이 완료되었습니다."));
    }

    @Override
    @PostMapping("/login")
    public ResponseEntity<CommonResponse<String>> login(
            @RequestBody @Valid LoginRequest loginRequest,
            HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        userService.login(loginRequest,session,request);

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(CommonResponse.ok("정상 로그인처리 되었습니다."));
    }

    @Override
    @PostMapping("/profile-setup")
    public ResponseEntity<?> setupProfile(
            @RequestBody @Valid ProfileUpdateRequest request,
            HttpSession session
    ) {
        UserSession user = (UserSession) session.getAttribute("LOGIN_USER");

        if (user == null) {
            throw new BusinessException(ErrorCode.SESSION_NOT_FOUND, "세션이 만료되었습니다.");
        }

        userService.completeInitialProfile(user.getUserId(), request, session);

        return ResponseEntity.ok(CommonResponse.ok("프로필 설정이 완료되었습니다."));
    }

    @Override
    @GetMapping("/me")
    public ResponseEntity<CommonResponse<ProfileResponse>> getMyProfile(HttpSession session) {
        UserSession sessionUser = (UserSession) session.getAttribute("LOGIN_USER");

        if (sessionUser == null) {
            throw new BusinessException(ErrorCode.SESSION_NOT_FOUND);
        }

        ProfileResponse response = userService.getUserProfile(sessionUser.getUserId());
        return ResponseEntity.ok(CommonResponse.ok(response));
    }
}
