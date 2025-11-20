package com.finance_control.shared.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.PageableHandlerMethodArgumentResolverCustomizer;

/**
 * Pagination configuration that uses environment variables through AppProperties.
 * Configures pagination settings like default page size and max page size from environment variables.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(AppProperties.class)
public class PaginationConfig {

    private final AppProperties appProperties;

    @Bean
    public PageableHandlerMethodArgumentResolverCustomizer paginationCustomizer() {
        com.finance_control.shared.config.properties.PaginationProperties pagination = appProperties.pagination();

        log.info("Configuring pagination - DefaultSize: {}, MaxSize: {}, DefaultSort: {}, DefaultDirection: {}",
                pagination.defaultPageSize(),
                pagination.maxPageSize(),
                pagination.defaultSort(),
                pagination.defaultDirection());

        return pageableResolver -> {
            pageableResolver.setMaxPageSize(pagination.maxPageSize());
            pageableResolver.setPageParameterName("page");
            pageableResolver.setSizeParameterName("size");
        };
    }
}
