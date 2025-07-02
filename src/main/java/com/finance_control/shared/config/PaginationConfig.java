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
        AppProperties.Pagination pagination = appProperties.getPagination();
        
        log.info("Configuring pagination - DefaultSize: {}, MaxSize: {}, DefaultSort: {}, DefaultDirection: {}", 
                pagination.getDefaultPageSize(),
                pagination.getMaxPageSize(),
                pagination.getDefaultSort(),
                pagination.getDefaultDirection());
        
        return pageableResolver -> {
            pageableResolver.setMaxPageSize(pagination.getMaxPageSize());
            pageableResolver.setPageParameterName("page");
            pageableResolver.setSizeParameterName("size");
        };
    }
} 