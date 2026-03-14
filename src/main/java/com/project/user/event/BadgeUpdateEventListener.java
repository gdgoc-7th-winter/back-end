package com.project.user.event;

import com.project.global.error.BusinessException;
import com.project.global.error.ErrorCode;
import com.project.user.domain.entity.LevelBadge;
import com.project.user.domain.entity.User;
import com.project.user.domain.repository.LevelBadgeRepository;
import com.project.user.domain.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;


@Component
@RequiredArgsConstructor
public class BadgeUpdateEventListener {

    private final LevelBadgeRepository levelBadgeRepository;
    private final UserRepository userRepository;

    @EventListener
    @Transactional
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePointChanged(UserPointChangeEvent event) {
        User user = userRepository.findById(event.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "사용자 없음"));

        LevelBadge newBadge = levelBadgeRepository.findByPointWithinRange(event.getCurrentPoint())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "해당 점수 구간 뱃지 없음"));
        
        user.updateBadge(newBadge);
    }
}
