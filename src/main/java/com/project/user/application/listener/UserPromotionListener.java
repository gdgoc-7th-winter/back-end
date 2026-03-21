package com.project.user.application.listener;

import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.user.application.dto.UserSession;
import com.project.global.event.Impl.UserPromotionEvent;
import com.project.user.application.service.UserService;
import com.project.user.domain.entity.User;
import com.project.user.domain.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserPromotionListener {
    private final UserService userService;
    private final UserRepository userRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserPromotion(UserPromotionEvent event) {
        log.info("이벤트 수신 완료: userId={}, referenceId={}", event.getUserId(), event.getReferenceId());

        User user = Optional.ofNullable(userRepository.findById(event.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "회원 정보를 찾을 수 없습니다."))).get();

        UserSession updatedSession = UserSession.builder()
                .userId(user.getId())
                .authority(user.getAuthority())
                .needsProfile(user.needsInitialSetup())
                .build();

        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attr != null) {
            HttpSession session = attr.getRequest().getSession();
            session.setAttribute("LOGIN_USER", updatedSession);
        }
        userService.updateSecurityContext(event.userId());
    }
}
