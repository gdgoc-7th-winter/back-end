package com.project.post.domain.repository.querydsl;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringExpression;

/**
 * QueryDSL에서 LIKE 검색 시 와일드카드 이스케이프 및 대소문자 무시 부분 일치 조건을 제공합니다.
 */
public final class QuerydslLikeExpressions {

    private QuerydslLikeExpressions() {
    }

    public static String escapeLikeWildcard(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        return value
                .replace("!", "!!")
                .replace("%", "!%")
                .replace("_", "!_");
    }

    public static BooleanExpression likeIgnoreCaseContains(StringExpression expr, String escapedKeyword) {
        return Expressions.booleanTemplate(
                "LOWER({0}) LIKE LOWER(CONCAT('%', {1}, '%')) ESCAPE '!'",
                expr,
                escapedKeyword
        );
    }
}
