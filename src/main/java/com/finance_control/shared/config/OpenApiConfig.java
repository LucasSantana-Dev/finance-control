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

/**
 * OpenAPI configuration that uses environment variables through AppProperties.
 * Configures Swagger UI and API documentation with settings from environment variables.
 */
@Slf4j
// @Configuration  // Temporarily disabled to test SpringDoc auto-configuration
@RequiredArgsConstructor
public class OpenApiConfig {

    private final AppProperties appProperties;

    @Bean
    public OpenAPI customOpenAPI() {
        AppProperties.OpenApi openApi = appProperties.openApi();

        log.info("Configuring OpenAPI with title: {}, version: {}",
                openApi.title(), openApi.version());

        return new OpenAPI()
                .info(new Info()
                        .title(openApi.title())
                        .description(openApi.description())
                        .version(openApi.version())
                        .contact(new Contact()
                                .name(openApi.contactName())
                                .email(openApi.contactEmail())
                                .url(openApi.contactUrl()))
                        .license(new License()
                                .name(openApi.licenseName())
                                .url(openApi.licenseUrl())))
                .addServersItem(new Server()
                        .url(openApi.serverUrl())
                        .description(openApi.serverDescription()))
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
