package com.project.algo.domain.repository.impl;

import com.project.algo.domain.entity.DailyChallenge;
import com.project.algo.domain.enums.AlgorithmTag;
import com.project.algo.domain.repository.DailyChallengeRepositoryCustom;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DailyChallengeRepositoryImpl implements DailyChallengeRepositoryCustom {

    private final EntityManager em;

    @Override
    public Page<DailyChallenge> searchChallenges(String keyword, List<AlgorithmTag> tags, Pageable pageable) {
        boolean hasKeyword = keyword != null && !keyword.isBlank();
        boolean hasTags    = tags != null && !tags.isEmpty();

        String baseCondition = buildCondition(hasKeyword, hasTags);

        String jpql      = "SELECT dc FROM DailyChallenge dc JOIN FETCH dc.author WHERE " + baseCondition + " ORDER BY dc.createdAt DESC";
        String countJpql = "SELECT COUNT(dc) FROM DailyChallenge dc WHERE " + baseCondition;

        TypedQuery<DailyChallenge> query      = em.createQuery(jpql, DailyChallenge.class);
        TypedQuery<Long>           countQuery = em.createQuery(countJpql, Long.class);

        bindParams(query,      hasKeyword, hasTags, keyword, tags);
        bindParams(countQuery, hasKeyword, hasTags, keyword, tags);

        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<DailyChallenge> content = query.getResultList();
        long total = countQuery.getSingleResult();

        return new PageImpl<>(content, pageable, total);
    }

    private String buildCondition(boolean hasKeyword, boolean hasTags) {
        StringBuilder sb = new StringBuilder("1=1");
        if (hasKeyword) {
            sb.append(" AND (LOWER(dc.title) LIKE :keyword OR LOWER(dc.problemNumber) LIKE :keyword)");
        }
        if (hasTags) {
            sb.append(" AND dc.id IN (SELECT dc2.id FROM DailyChallenge dc2 JOIN dc2.algorithmTags t WHERE t IN :tags)");
        }
        return sb.toString();
    }

    private void bindParams(TypedQuery<?> query, boolean hasKeyword, boolean hasTags,
                            String keyword, List<AlgorithmTag> tags) {
        if (hasKeyword) {
            query.setParameter("keyword", "%" + keyword.toLowerCase() + "%");
        }
        if (hasTags) {
            query.setParameter("tags", tags);
        }
    }
}
