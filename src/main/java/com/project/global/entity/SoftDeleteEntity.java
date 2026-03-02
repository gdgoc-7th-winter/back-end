package com.project.global.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import java.time.Instant;

@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@MappedSuperclass
public abstract class SoftDeleteEntity extends AuditEntity {

    @Column(columnDefinition = "TIMESTAMPTZ")
    private Instant deletedAt;

    public void softDelete() {
        this.deletedAt = Instant.now();
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public void restore() {
        this.deletedAt = null;
    }
}
