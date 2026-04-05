package com.project.global.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

/**
 * JPA Auditing({@link org.springframework.data.jpa.domain.support.AuditingEntityListener}) 없이
 * {@code created_at} / {@code updated_at}을 애플리케이션 코드에서 직접 관리할 때 사용한다.
 * 트랜잭셔널 아웃박스, 알림 발송 큐, 작업 큐 등 상태 전이마다 시각을 명시적으로 찍는 테이블에 두기 적합하다.
 */
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@MappedSuperclass
public abstract class ManualTimestampEntity extends BaseEntity {

    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMPTZ")
    protected Instant createdAt;

    @Column(name = "updated_at", nullable = false, columnDefinition = "TIMESTAMPTZ")
    protected Instant updatedAt;
}
