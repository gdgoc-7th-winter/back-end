package com.project.contribution.domain.support;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IdempotencyKeysTest {

    @Test
    @DisplayName("GRANT 키 형식")
    void grantMatchesMigrationFormat() {
        assertThat(IdempotencyKeys.grant(77L, 3L, 123L))
                .isEqualTo("GRANT/user:77/score:3/ref:123");
    }

    @Test
    @DisplayName("REVOKE 키는 GRANT와 구분")
    void revokeUsesDistinctNamespace() {
        String key = IdempotencyKeys.revoke(77L, 3L, 123L, "POST_DELETED");
        assertThat(key).isEqualTo("REVOKE/user:77/score:3/ref:123/reason:POST_DELETED");
        assertThat(key).isNotEqualTo(IdempotencyKeys.grant(77L, 3L, 123L));
    }
}
