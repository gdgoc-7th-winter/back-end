package com.project.ranking.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "ranking")
public class RankingProperties {

    private int maxPageSize = 100;

    private int defaultPageSize = 20;

    private boolean useWindowRankSql = true;
}
