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


@Component
@RequiredArgsConstructor
public class BadgeUpdateEventListener {

    private final LevelBadgeRepository levelBadgeRepository;
    private final UserRepository userRepository;

    @PostConstruct
    public void init() {
        System.out.println(">>> BadgeUpdateEventListener 빈이 등록되었습니다.");
    }

    @EventListener
    public void handlePointChanged(UserPointChangeEvent event) {
        User user = userRepository.findById(event.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "사용자 없음"));

        LevelBadge newBadge = levelBadgeRepository.findByPointWithinRange(event.getCurrentPoint())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "해당 점수 구간 뱃지 없음"));
        
        user.updateBadge(newBadge);
    }
}
