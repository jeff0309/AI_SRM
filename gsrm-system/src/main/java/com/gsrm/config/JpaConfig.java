package com.gsrm.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * JPA 配置類別.
 *
 * <p>啟用 JPA Auditing、Repository 掃描，與事務管理。</p>
 *
 * @author Jeff
 * @since 2026-03-06
 */
@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "com.gsrm.repository")
@EnableTransactionManagement
public class JpaConfig {
    // Spring Boot 自動配置已處理大部分 JPA 設定。
    // 此類別僅用於明確啟用 Auditing 與 Repository 掃描。
}
