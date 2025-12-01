package com.finance_control;

import com.finance_control.shared.config.AppProperties;
import com.finance_control.shared.config.properties.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = {
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.http.client.HttpClientAutoConfiguration,org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration"
})
@ActiveProfiles("test")
class FinanceControlApplicationTests {

	@Test
	void contextLoads() {
		// This test verifies that the Spring application context loads successfully
		// without any configuration errors. The test passes if the context starts up.
	}

	@TestConfiguration
	static class TestConfig {
		@Bean
		@Primary
		public AppProperties appProperties() {
			return new AppProperties(
				false,
				new DatabaseProperties(
					"jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
					"sa",
					"",
					"org.h2.Driver",
					"",
					"testdb",
					new DatabaseProperties.PoolProperties(2, 5, 1, 300000, 10000, 300000, 60000)
				),
				new SecurityProperties(
					new SecurityProperties.JwtProperties("testSecretKeyWithMinimumLengthOf256BitsForJWT", 86400000L, 604800000L, "test", "test"),
					new SecurityProperties.CorsProperties(java.util.List.of(), java.util.List.of(), java.util.List.of(), false, 0),
					java.util.List.of("/api/auth/**", "/api/users", "/monitoring/**", "/api/monitoring/**", "/actuator/**"),
					new SecurityProperties.EncryptionProperties()
				),
				new ServerProperties(0, "", "/", 8192, 2097152, 10000, 15000, 15000),
				new LoggingProperties("DEBUG", "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n", "logs", "test.log", "test-error.log", 5242880, 7, 256, false),
				new JpaProperties("create-drop", "org.hibernate.dialect.H2Dialect", false, false, false, "org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl", false, new JpaProperties.HibernateProperties()),
				new FlywayProperties(false, java.util.List.of("classpath:db/migration"), "false", "0", "true", "false", "true", "false"),
				new ActuatorProperties(false, java.util.List.of("health"), "/actuator", false, false, false),
				new OpenApiProperties("Test API", "Test API Description", "1.0.0-test", "Test Contact", "test@test.com", "https://test.com", "MIT", "https://test.com", "http://localhost:8080", "Test server"),
				new PaginationProperties(5, 20, "id", "ASC"),
				new RedisProperties("localhost", 6379, "", 0, 2000, new RedisProperties.RedisPoolProperties(8, 8, 0, -1)),
				new CacheProperties(true, 3600L, 1800L, 900L),
				new RateLimitProperties(true, 100, 200, 60),
				new AiProperties(),
				new SupabaseProperties(false, "", "", "", "",
					new SupabaseProperties.SupabaseDatabaseProperties(false, "", 5432, "", "", "", false, "require"),
					new SupabaseProperties.StorageProperties(false, "avatars", "documents", "transactions", new SupabaseProperties.CompressionProperties(true, 6, 0.1, 1024, java.util.List.of())),
					new SupabaseProperties.RealtimeProperties(false, java.util.List.of("transactions", "dashboard", "goals"))),
				new MonitoringProperties(true, new MonitoringProperties.SentryProperties(false, "test-dsn", "ERROR", "1.0.0", 0.1, 0.1, false, true, true)),
				new OpenFinanceProperties(),
				new FeatureFlagsProperties()
			);
		}
	}

}
