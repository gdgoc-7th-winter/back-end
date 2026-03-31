package com.project.contribution.application.service.support;

import com.project.user.domain.entity.LevelBadge;
import com.project.user.domain.entity.User;
import com.project.user.domain.service.LevelBadgeResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class UserLevelBadgeApplier {

    private final LevelBadgeResolver levelBadgeResolver;

    public void applyIfNeeded(User user) {
        LevelBadge resolved = levelBadgeResolver.resolveForTotalPoints(user.getTotalPoint());
        LevelBadge current = user.getLevelBadge();
        if (current == null) {
            user.updateBadge(resolved);
            return;
        }
        if (!Objects.equals(current.getId(), resolved.getId())) {
            user.updateBadge(resolved);
        }
    }
}
