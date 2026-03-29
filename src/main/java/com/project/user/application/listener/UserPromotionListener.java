package com.project.user.application.listener;

import com.project.global.event.impl.UserPromotionEvent;
import com.project.user.application.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserPromotionListener {
    private final UserService userService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserPromotion(UserPromotionEvent event) {
        log.info("이벤트 수신 완료: userId={}, referenceId={}", event.getUserId(), event.getReferenceId());
        userService.updateSecurityContext(event.userId());
    }
}
