package com.project.global.presentation;

import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.global.response.CommonResponse;
import com.project.user.application.dto.UserSession;
import com.project.user.application.service.UserService;
import com.project.user.domain.enums.Authority;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 개발/테스트 전용 컨트롤러 — prod 프로파일에서는 Bean 등록 자체가 비활성화됨
 */
@Profile("!prod")
@RestController
@RequestMapping("/api/v1/dev")
@RequiredArgsConstructor
public class DevController {

    private final UserService userService;

    @PostMapping("/grant-authority")
    public ResponseEntity<CommonResponse<String>> grantAuthority(
            @RequestParam Authority authority,
            HttpSession session
    ) {
        UserSession loginUser = (UserSession) session.getAttribute("LOGIN_USER");
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.SESSION_NOT_FOUND, "로그인이 필요합니다.");
        }
        userService.grantAuthority(loginUser.getUserId(), authority);
        return ResponseEntity.ok(CommonResponse.ok(
                "권한이 " + authority.name() + "으로 변경되었습니다."
        ));
    }
}
