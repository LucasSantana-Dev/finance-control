package com.finance_control.shared.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI configuration that uses environment variables through AppProperties.
 * Configures Swagger UI and API documentation with settings from environment variables.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class OpenApiConfig {
    
    private final AppProperties appProperties;
    
    @Bean
    public OpenAPI customOpenAPI() {
        AppProperties.OpenApi openApi = appProperties.getOpenApi();
        
        log.info("Configuring OpenAPI with title: {}, version: {}", 
                openApi.getTitle(), openApi.getVersion());
        
        return new OpenAPI()
                .info(new Info()
                        .title(openApi.getTitle())
                        .description(openApi.getDescription())
                        .version(openApi.getVersion())
                        .contact(new Contact()
                                .name(openApi.getContactName())
                                .email(openApi.getContactEmail())
                                .url(openApi.getContactUrl()))
                        .license(new License()
                                .name(openApi.getLicenseName())
                                .url(openApi.getLicenseUrl())))
                .addServersItem(new Server()
                        .url(openApi.getServerUrl())
                        .description(openApi.getServerDescription()))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT Authorization header using the Bearer scheme. " +
                                        "Enter 'Bearer' [space] and then your token in the text input below.")));
    }
} 