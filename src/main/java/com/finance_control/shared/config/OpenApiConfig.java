package com.finance_control.shared.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI financeControlOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Finance Control API")
                        .description("API documentation for the Finance Control application")
                        .version("v1.0.0")
                        .contact(new Contact()
                            .name("Finance Control Team")
                            .email("lucassantana@gmail.com"))
                        .license(new License().name("Apache 2.0").url("http://springdoc.org")))
                .externalDocs(new ExternalDocumentation()
                        .description("Project Repository")
                        .url("https://github.com/LucasSantana-Dev/finance-control"));
    }
} 