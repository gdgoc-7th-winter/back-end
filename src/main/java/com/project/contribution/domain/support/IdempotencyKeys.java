package com.project.contribution.domain.support;

public final class IdempotencyKeys {

    private IdempotencyKeys() {
    }

    public static String grant(Long userId, Long contributionScoreId, Long referenceId) {
        return "GRANT/user:%d/score:%d/ref:%d".formatted(userId, contributionScoreId, referenceId);
    }

    public static String revoke(Long userId, Long contributionScoreId, Long referenceId, String reasonToken) {
        String safe = sanitizeToken(reasonToken);
        return "REVOKE/user:%d/score:%d/ref:%d/reason:%s".formatted(userId, contributionScoreId, referenceId, safe);
    }

    private static String sanitizeToken(String reasonToken) {
        if (reasonToken == null || reasonToken.isBlank()) {
            return "UNKNOWN";
        }
        return reasonToken.replace('/', '_').replace(':', '_').trim();
    }
}
