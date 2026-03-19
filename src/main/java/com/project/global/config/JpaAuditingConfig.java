package com.project.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.lang.NonNull;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {

    @Bean
    public TransactionTemplate transactionTemplate(@NonNull PlatformTransactionManager transactionManager) {
        return new TransactionTemplate(transactionManager);
    }
}
