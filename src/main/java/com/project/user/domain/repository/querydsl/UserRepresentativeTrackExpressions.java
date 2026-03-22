package com.project.user.domain.repository.querydsl;

import com.project.user.domain.entity.QTrack;
import com.project.user.domain.entity.QUser;
import com.project.user.domain.entity.QUserTrack;
import com.querydsl.core.types.Expression;
import com.querydsl.jpa.JPAExpressions;

/**
 * 목록/상세 조회 등에서 작성자 대표 트랙명을 projection할 때 사용하는 QueryDSL 식
 */
public final class UserRepresentativeTrackExpressions {

    private UserRepresentativeTrackExpressions() {
    }

    public static Expression<String> representativeTrackNameSubquery(QUser user) {
        QUserTrack ut = new QUserTrack("utRep");
        QTrack tr = new QTrack("trRep");
        QUserTrack utMin = new QUserTrack("utMin");
        return JPAExpressions.select(tr.name)
                .from(ut)
                .join(ut.track, tr)
                .where(
                        ut.user.id.eq(user.id),
                        ut.id.eq(
                                JPAExpressions.select(utMin.id.min())
                                        .from(utMin)
                                        .where(
                                                utMin.user.id.eq(user.id),
                                                utMin.track.isNotNull()
                                        )
                        )
                );
    }
}
