package com.project.contribution.application.service.support;

import com.project.user.event.UserPointChangeEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserPointChangeNotifier {

    private final ApplicationEventPublisher eventPublisher;

    public void notifyPointChanged(Long userId, int newTotalPoint) {
        eventPublisher.publishEvent(new UserPointChangeEvent(userId, newTotalPoint));
    }
}
