package com.project.user.application.listener;

import com.project.user.application.dto.UserSession;
import com.project.global.event.Impl.UserPromotionEvent;
import com.project.user.application.service.UserService;
import com.project.user.domain.enums.Authority;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserPromotionListener {
    private final UserService userService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserPromotion(UserPromotionEvent event) {
        log.info("이벤트 수신 완료: userId={}, referenceId={}", event.getUserId(), event.getReferenceId());

        UserSession updatedSession = UserSession.builder()
                .userId(event.getUserId())
                .authority(Authority.USER)
                .needsProfile(false)
                .build();

        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attr != null) {
            HttpSession session = attr.getRequest().getSession();
            // 세션 업데이트 로직
            session.setAttribute("LOGIN_USER", updatedSession);
        }

        userService.updateSecurityContext(event.userId());
    }
}
