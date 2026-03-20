package com.project.user.presentation.controller;

import com.project.user.application.service.UserService;
import com.project.user.presentation.dto.request.PasswordUpdateRequest;
import com.project.user.presentation.swagger.UserControllerDocs;
import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.global.response.CommonResponse;

import com.project.user.application.dto.UserSession;

import com.project.user.presentation.dto.request.LoginRequest;
import com.project.user.presentation.dto.request.ProfileUpdateRequest;
import com.project.user.presentation.dto.request.SignUpRequest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



@Slf4j
@RestController
@RequestMapping("/api/v1/users")
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
    public ResponseEntity<CommonResponse<String>> login(@RequestBody @Valid LoginRequest loginRequest) {
        userService.login(loginRequest.email(), loginRequest.password());
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(CommonResponse.ok("정상 로그인처리 되었습니다."));
    }


    @Override
    @PostMapping("/profile-setup")
    public ResponseEntity<CommonResponse<String>> setupProfile(
            @RequestBody @Valid ProfileUpdateRequest request,
            HttpSession session
    ) {
        UserSession user = (UserSession) session.getAttribute("LOGIN_USER");

        if (user == null) {
            throw new BusinessException(ErrorCode.SESSION_NOT_FOUND, "세션이 만료되었습니다.");
        }

        userService.updateProfile(user.getUserId(), request);

        return ResponseEntity.ok(CommonResponse.ok("프로필 설정이 완료되었습니다."));
    }

    @PostMapping("/change-password")
    public ResponseEntity<CommonResponse<String>> changePassword(
            PasswordUpdateRequest request,
            HttpSession session) {
        UserSession sessionUser = (UserSession) session.getAttribute("LOGIN_USER");

        if (sessionUser == null) {
            throw new BusinessException(ErrorCode.SESSION_NOT_FOUND);
        }

        userService.changePassword(sessionUser.getUserId(), request);

        return ResponseEntity.ok(CommonResponse.ok("비밀번호가 변경되었습니다."));
    }

    @GetMapping("/logout")
    public ResponseEntity<CommonResponse<String>> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        userService.logout(session);
        return ResponseEntity.ok(CommonResponse.ok("정상적으로 로그아웃 처리되었습니다."));
    }

}
