package com.project.user.domain.repository.querydsl;

import com.project.user.domain.entity.QUser;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.CaseBuilder;

public final class UserWithdrawnExpressions {

    private UserWithdrawnExpressions() {
    }

    public static Expression<Boolean> authorIsWithdrawn(QUser user) {
        return new CaseBuilder()
                .when(user.deletedAt.isNotNull()).then(true)
                .otherwise(false);
    }
}
