package com.project.user.presentation.controller;

import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.global.response.CommonResponse;
import com.project.user.application.dto.UserSession;
import com.project.user.application.dto.response.ProfileResponse;
import com.project.user.application.service.UserService;
import com.project.user.presentation.dto.request.ProfilePatchRequest;
import com.project.user.presentation.swagger.ProfileControllerDocs;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me")
@RequiredArgsConstructor
public class ProfileController implements ProfileControllerDocs {

    private final UserService userService;

    @Override
    @GetMapping("/profile")
    public ResponseEntity<CommonResponse<ProfileResponse>> getMyProfile(HttpSession session) {
        UserSession sessionUser = (UserSession) session.getAttribute("LOGIN_USER");

        if (sessionUser == null) {
            throw new BusinessException(ErrorCode.SESSION_NOT_FOUND);
        }

        ProfileResponse response = userService.getUserProfile(sessionUser.getUserId());
        return ResponseEntity.ok(CommonResponse.ok(response));
    }

    @Override
    @PatchMapping("/profile")
    public ResponseEntity<CommonResponse<String>> patchProfile(
            @RequestBody ProfilePatchRequest request,
            HttpSession session) {
        UserSession sessionUser = (UserSession) session.getAttribute("LOGIN_USER");

        if (sessionUser == null) {
            throw new BusinessException(ErrorCode.SESSION_NOT_FOUND);
        }

        userService.patchProfile(sessionUser.getUserId(), request);
        return ResponseEntity.ok(CommonResponse.ok("프로필이 수정되었습니다."));
    }

    @Override
    @DeleteMapping("/profile")
    public ResponseEntity<CommonResponse<String>> deleteAccount(HttpSession session) {
        UserSession sessionUser = (UserSession) session.getAttribute("LOGIN_USER");

        if (sessionUser == null) {
            throw new BusinessException(ErrorCode.SESSION_NOT_FOUND);
        }

        userService.deleteUser(sessionUser.getUserId());
        return ResponseEntity.ok(CommonResponse.ok("회원 탈퇴가 완료되었습니다."));
    }
}
