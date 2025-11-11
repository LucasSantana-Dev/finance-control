package com.finance_control.unit.shared.controller;

import com.finance_control.shared.controller.ConfigurationController;
import com.finance_control.shared.service.ConfigurationService;
import com.finance_control.shared.security.CustomUserDetails;
import com.finance_control.users.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ConfigurationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ConfigurationService configurationService;

    private CustomUserDetails testUserDetails;

    @BeforeEach
    void setUp() {
        User testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setIsActive(true);
        testUserDetails = new CustomUserDetails(testUser);
    }

    @Test
    void getAllConfiguration_ShouldReturnAllConfig() throws Exception {
        Map<String, Object> config = new HashMap<>();
        config.put("database", Map.of("url", "jdbc:postgresql://localhost:5432/finance"));
        config.put("server", Map.of("port", 8080));
        when(configurationService.getAllConfig()).thenReturn(config);

        mockMvc.perform(get("/api/config")
                .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.database").exists())
                .andExpect(jsonPath("$.server").exists());

        verify(configurationService, times(1)).getAllConfig();
    }

    @Test
    void getDatabaseConfiguration_ShouldReturnDatabaseConfig() throws Exception {
        Map<String, Object> dbConfig = new HashMap<>();
        dbConfig.put("url", "jdbc:postgresql://localhost:5432/finance");
        dbConfig.put("port", 5432);
        when(configurationService.getDatabaseConfig()).thenReturn(dbConfig);

        mockMvc.perform(get("/api/config/database")
                .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.url").value("jdbc:postgresql://localhost:5432/finance"))
                .andExpect(jsonPath("$.port").value(5432));

        verify(configurationService, times(1)).getDatabaseConfig();
    }

    @Test
    void getSecurityConfiguration_ShouldReturnSecurityConfig() throws Exception {
        Map<String, Object> securityConfig = new HashMap<>();
        securityConfig.put("jwt", Map.of("secret", "test-secret", "expiration", 3600));
        when(configurationService.getSecurityConfig()).thenReturn(securityConfig);

        mockMvc.perform(get("/api/config/security")
                .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.jwt").exists());

        verify(configurationService, times(1)).getSecurityConfig();
    }

    @Test
    void getServerConfiguration_ShouldReturnServerConfig() throws Exception {
        Map<String, Object> serverConfig = new HashMap<>();
        serverConfig.put("port", 8080);
        serverConfig.put("contextPath", "/api");
        when(configurationService.getServerConfig()).thenReturn(serverConfig);

        mockMvc.perform(get("/api/config/server")
                .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.port").value(8080))
                .andExpect(jsonPath("$.contextPath").value("/api"));

        verify(configurationService, times(1)).getServerConfig();
    }

    @Test
    void getLoggingConfiguration_ShouldReturnLoggingConfig() throws Exception {
        Map<String, Object> loggingConfig = new HashMap<>();
        loggingConfig.put("level", "INFO");
        loggingConfig.put("file", "logs/application.log");
        when(configurationService.getLoggingConfig()).thenReturn(loggingConfig);

        mockMvc.perform(get("/api/config/logging")
                .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.level").value("INFO"));

        verify(configurationService, times(1)).getLoggingConfig();
    }

    @Test
    void getJpaConfiguration_ShouldReturnJpaConfig() throws Exception {
        Map<String, Object> jpaConfig = new HashMap<>();
        jpaConfig.put("showSql", false);
        jpaConfig.put("formatSql", true);
        when(configurationService.getJpaConfig()).thenReturn(jpaConfig);

        mockMvc.perform(get("/api/config/jpa")
                .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.showSql").value(false));

        verify(configurationService, times(1)).getJpaConfig();
    }

    @Test
    void getFlywayConfiguration_ShouldReturnFlywayConfig() throws Exception {
        Map<String, Object> flywayConfig = new HashMap<>();
        flywayConfig.put("enabled", true);
        flywayConfig.put("locations", "classpath:db/migration");
        when(configurationService.getFlywayConfig()).thenReturn(flywayConfig);

        mockMvc.perform(get("/api/config/flyway")
                .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.enabled").value(true));

        verify(configurationService, times(1)).getFlywayConfig();
    }

    @Test
    void getActuatorConfiguration_ShouldReturnActuatorConfig() throws Exception {
        Map<String, Object> actuatorConfig = new HashMap<>();
        actuatorConfig.put("enabled", true);
        actuatorConfig.put("basePath", "/actuator");
        when(configurationService.getActuatorConfig()).thenReturn(actuatorConfig);

        mockMvc.perform(get("/api/config/actuator")
                .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.enabled").value(true));

        verify(configurationService, times(1)).getActuatorConfig();
    }

    @Test
    void getOpenApiConfiguration_ShouldReturnOpenApiConfig() throws Exception {
        Map<String, Object> openApiConfig = new HashMap<>();
        openApiConfig.put("enabled", true);
        openApiConfig.put("path", "/api-docs");
        when(configurationService.getOpenApiConfig()).thenReturn(openApiConfig);

        mockMvc.perform(get("/api/config/openapi")
                .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.enabled").value(true));

        verify(configurationService, times(1)).getOpenApiConfig();
    }

    @Test
    void getPaginationConfiguration_ShouldReturnPaginationConfig() throws Exception {
        Map<String, Object> paginationConfig = new HashMap<>();
        paginationConfig.put("defaultPageSize", 20);
        paginationConfig.put("maxPageSize", 100);
        when(configurationService.getPaginationConfig()).thenReturn(paginationConfig);

        mockMvc.perform(get("/api/config/pagination")
                .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.defaultPageSize").value(20));

        verify(configurationService, times(1)).getPaginationConfig();
    }

    @Test
    void getEnvironmentInfo_ShouldReturnEnvironmentInfo() throws Exception {
        Map<String, Object> envInfo = new HashMap<>();
        envInfo.put("name", "test");
        envInfo.put("version", "1.0.0");
        when(configurationService.getEnvironmentInfo()).thenReturn(envInfo);

        mockMvc.perform(get("/api/config/environment")
                .with(user(testUserDetails)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("test"));

        verify(configurationService, times(1)).getEnvironmentInfo();
    }
}
