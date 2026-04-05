package com.project.contribution.application.support;

import com.project.contribution.application.dto.ActivityContext;

/**
 * Outbox 중복 적재 방지용 키. 동일 활동 재시도 시 UK 위반으로 무시한다.
 */
public final class ActivityContextDedupKeys {

    private ActivityContextDedupKeys() {
    }

    public static String of(ActivityContext c) {
        long actor = c.actorUserId() != null ? c.actorUserId() : 0L;
        String scorePart = c.scoreCodeOverride() != null ? "/" + c.scoreCodeOverride() : "";
        return "%s/%d/%s/%d/%d%s"
                .formatted(c.activityType().name(), c.subjectUserId(), c.referenceKind().name(), c.referenceId(), actor, scorePart);
    }
}
