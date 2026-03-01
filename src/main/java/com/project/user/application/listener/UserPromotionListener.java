package com.project.user.application.listener;

import com.project.user.application.dto.UserSession;
import com.project.user.application.dto.request.UserPromotionEvent;
import com.project.user.application.service.UserService;
import com.project.user.domain.enums.Authority;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class UserPromotionListener {

    private final UserService userService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserPromotion(UserPromotionEvent event) {
        UserSession updatedSession = UserSession.builder()
                .userId(event.userId())
                .email(event.email())
                .authority(Authority.USER)
                .needsProfile(false)
                .build();
        event.session().setAttribute("LOGIN_USER", updatedSession);

        userService.updateSecurityContext(event.email());
    }
}
