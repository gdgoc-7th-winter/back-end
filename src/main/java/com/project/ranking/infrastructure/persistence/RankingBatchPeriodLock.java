package com.project.ranking.infrastructure.persistence;

import com.project.ranking.domain.RankingPeriodType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * 동일 {@code (period_type, period_key)} 배치가 분산 환경에서 동시에 돌지 않도록
 * PostgreSQL {@code pg_advisory_xact_lock}으로 트랜잭션 범위 락을 잡는다.
 * <p>
 * H2 등 PostgreSQL이 아닌 경우는 no-op(테스트 단일 JVM).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RankingBatchPeriodLock {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final DataSource dataSource;

    private volatile Boolean postgres;

    /**
     * 현재 트랜잭션에 묶인 advisory lock. 커밋/롤백 시 자동 해제.
     */
    public void acquire(RankingPeriodType periodType, String periodKey) {
        if (!isPostgreSql()) {
            return;
        }
        String k1 = periodType.name() + ":" + periodKey + "|a";
        String k2 = periodType.name() + ":" + periodKey + "|b";
        namedParameterJdbcTemplate.getJdbcTemplate().execute((ConnectionCallback<Void>) con -> {
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT pg_advisory_xact_lock(hashtext(?::text), hashtext(?::text))")) {
                ps.setString(1, k1);
                ps.setString(2, k2);
                ps.execute();
            }
            return null;
        });
    }

    private boolean isPostgreSql() {
        Boolean cached = postgres;
        if (cached != null) {
            return cached;
        }
        synchronized (this) {
            if (postgres != null) {
                return postgres;
            }
            try (Connection c = dataSource.getConnection()) {
                postgres = "PostgreSQL".equalsIgnoreCase(c.getMetaData().getDatabaseProductName());
            } catch (SQLException e) {
                log.warn("DB 제품명 확인 실패 — 랭킹 배치 advisory lock 비활성: {}", e.getMessage());
                postgres = false;
            }
            return postgres;
        }
    }
}
