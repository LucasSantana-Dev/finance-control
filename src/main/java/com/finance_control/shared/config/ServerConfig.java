package com.finance_control.shared.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Server configuration that uses environment variables through AppProperties.
 * Configures server port, context path, and other server settings from environment variables.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(AppProperties.class)
public class ServerConfig {
    
    private final AppProperties appProperties;
    
    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> tomcatCustomizer() {
        return factory -> {
            AppProperties.Server server = appProperties.server();

            log.info("Configuring server - Port: {}, Context: {}, MaxHeaderSize: {}KB, MaxPostSize: {}KB",
                    server.port(),
                    server.contextPath(),
                    server.maxHttpHeaderSize() / 1024,
                    server.maxHttpPostSize() / 1024);

            factory.setPort(server.port());
            factory.setContextPath(server.contextPath());

            factory.addConnectorCustomizers(connector -> {
                connector.setMaxPostSize(server.maxHttpPostSize());
                connector.setProperty("maxHttpHeaderSize", String.valueOf(server.maxHttpHeaderSize()));
                connector.setProperty("connectionTimeout", String.valueOf(server.connectionTimeout()));
                connector.setProperty("readTimeout", String.valueOf(server.readTimeout()));
                connector.setProperty("writeTimeout", String.valueOf(server.writeTimeout()));
            });
        };
    }
} 