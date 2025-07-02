package com.finance_control.shared.controller;

import com.finance_control.shared.service.ConfigurationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Controller that exposes configuration information through REST endpoints.
 * Useful for debugging and monitoring application configuration.
 */
@Slf4j
@RestController
@RequestMapping("/api/config")
@RequiredArgsConstructor
@Tag(name = "Configuration", description = "Configuration management endpoints")
public class ConfigurationController {
    
    private final ConfigurationService configurationService;
    
    @GetMapping
    @Operation(summary = "Get all configuration", description = "Retrieves all application configuration information")
    public ResponseEntity<Map<String, Object>> getAllConfiguration() {
        log.debug("Retrieving all configuration information");
        Map<String, Object> config = configurationService.getAllConfig();
        return ResponseEntity.ok(config);
    }
    
    @GetMapping("/database")
    @Operation(summary = "Get database configuration", description = "Retrieves database configuration information")
    public ResponseEntity<Map<String, Object>> getDatabaseConfiguration() {
        log.debug("Retrieving database configuration");
        Map<String, Object> config = configurationService.getDatabaseConfig();
        return ResponseEntity.ok(config);
    }
    
    @GetMapping("/security")
    @Operation(summary = "Get security configuration", description = "Retrieves security configuration information")
    public ResponseEntity<Map<String, Object>> getSecurityConfiguration() {
        log.debug("Retrieving security configuration");
        Map<String, Object> config = configurationService.getSecurityConfig();
        return ResponseEntity.ok(config);
    }
    
    @GetMapping("/server")
    @Operation(summary = "Get server configuration", description = "Retrieves server configuration information")
    public ResponseEntity<Map<String, Object>> getServerConfiguration() {
        log.debug("Retrieving server configuration");
        Map<String, Object> config = configurationService.getServerConfig();
        return ResponseEntity.ok(config);
    }
    
    @GetMapping("/logging")
    @Operation(summary = "Get logging configuration", description = "Retrieves logging configuration information")
    public ResponseEntity<Map<String, Object>> getLoggingConfiguration() {
        log.debug("Retrieving logging configuration");
        Map<String, Object> config = configurationService.getLoggingConfig();
        return ResponseEntity.ok(config);
    }
    
    @GetMapping("/jpa")
    @Operation(summary = "Get JPA configuration", description = "Retrieves JPA configuration information")
    public ResponseEntity<Map<String, Object>> getJpaConfiguration() {
        log.debug("Retrieving JPA configuration");
        Map<String, Object> config = configurationService.getJpaConfig();
        return ResponseEntity.ok(config);
    }
    
    @GetMapping("/flyway")
    @Operation(summary = "Get Flyway configuration", description = "Retrieves Flyway configuration information")
    public ResponseEntity<Map<String, Object>> getFlywayConfiguration() {
        log.debug("Retrieving Flyway configuration");
        Map<String, Object> config = configurationService.getFlywayConfig();
        return ResponseEntity.ok(config);
    }
    
    @GetMapping("/actuator")
    @Operation(summary = "Get Actuator configuration", description = "Retrieves Actuator configuration information")
    public ResponseEntity<Map<String, Object>> getActuatorConfiguration() {
        log.debug("Retrieving Actuator configuration");
        Map<String, Object> config = configurationService.getActuatorConfig();
        return ResponseEntity.ok(config);
    }
    
    @GetMapping("/openapi")
    @Operation(summary = "Get OpenAPI configuration", description = "Retrieves OpenAPI configuration information")
    public ResponseEntity<Map<String, Object>> getOpenApiConfiguration() {
        log.debug("Retrieving OpenAPI configuration");
        Map<String, Object> config = configurationService.getOpenApiConfig();
        return ResponseEntity.ok(config);
    }
    
    @GetMapping("/pagination")
    @Operation(summary = "Get pagination configuration", description = "Retrieves pagination configuration information")
    public ResponseEntity<Map<String, Object>> getPaginationConfiguration() {
        log.debug("Retrieving pagination configuration");
        Map<String, Object> config = configurationService.getPaginationConfig();
        return ResponseEntity.ok(config);
    }
    
    @GetMapping("/environment")
    @Operation(summary = "Get environment information", description = "Retrieves environment information")
    public ResponseEntity<Map<String, Object>> getEnvironmentInfo() {
        log.debug("Retrieving environment information");
        Map<String, Object> info = configurationService.getEnvironmentInfo();
        return ResponseEntity.ok(info);
    }
} 