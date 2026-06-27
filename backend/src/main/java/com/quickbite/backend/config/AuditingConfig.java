package com.quickbite.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Enables JPA auditing so that @CreatedDate and @LastModifiedDate
 * on BaseEntity are automatically populated.
 */
@Configuration
@EnableJpaAuditing
public class AuditingConfig {
}
