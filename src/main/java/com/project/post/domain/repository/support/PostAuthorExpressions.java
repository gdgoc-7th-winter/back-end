package com.project.post.domain.repository.support;

import com.project.user.domain.entity.QTrack;
import com.project.user.domain.entity.QUser;
import com.project.user.domain.entity.QUserTrack;
import com.querydsl.core.types.Expression;
import com.querydsl.jpa.JPAExpressions;

public final class PostAuthorExpressions {

    private PostAuthorExpressions() {
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
                                        .where(utMin.user.id.eq(user.id))
                        )
                );
    }
}
