package com.project.ranking.config;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "ranking")
public class RankingProperties {

    @Min(1)
    private int maxPageSize = 100;

    @Min(1)
    private int defaultPageSize = 20;

    private boolean useWindowRankSql = true;

    @AssertTrue(message = "ranking.default-page-size는 ranking.max-page-size 이하여야 합니다.")
    public boolean isConsistentPageSizes() {
        return defaultPageSize <= maxPageSize;
    }
}
