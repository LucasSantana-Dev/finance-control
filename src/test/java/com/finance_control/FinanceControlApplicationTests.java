package com.finance_control;

import com.finance_control.shared.config.AppProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
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
				new AppProperties.Database(
					"jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
					"sa",
					"",
					"org.h2.Driver",
					"",
					"testdb",
					new AppProperties.Pool(2, 5, 1, 300000, 10000, 300000, 60000)
				),
				new AppProperties.Security(
					new AppProperties.Jwt("testSecretKeyWithMinimumLengthOf256BitsForJWT", 86400000L, 604800000L, "test", "test"),
					new AppProperties.Cors(java.util.List.of(), java.util.List.of(), java.util.List.of(), false, 0),
					java.util.List.of("/api/auth/**", "/api/users", "/monitoring/**", "/api/monitoring/**", "/actuator/**")
				),
				new AppProperties.Server(0, "", "/", 8192, 2097152, 10000, 15000, 15000),
				new AppProperties.Logging("DEBUG", "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n", "logs", "test.log", "test-error.log", 5242880, 7, 256, false),
				new AppProperties.Jpa("create-drop", "org.hibernate.dialect.H2Dialect", false, false, false, "org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl", false, new AppProperties.Properties()),
				new AppProperties.Flyway(false, java.util.List.of("classpath:db/migration"), "false", "0", "true", "false", "true", "false"),
				new AppProperties.Actuator(false, java.util.List.of("health"), "/actuator", false, false, false),
				new AppProperties.OpenApi("Test API", "Test API Description", "1.0.0-test", "Test Contact", "test@test.com", "https://test.com", "MIT", "https://test.com", "http://localhost:8080", "Test server"),
				new AppProperties.Pagination(5, 20, "id", "ASC"),
				new AppProperties.Redis("localhost", 6379, "", 0, 2000, new AppProperties.RedisPool(8, 8, 0, -1)),
				new AppProperties.Cache(true, 3600L, 1800L, 900L),
				new AppProperties.RateLimit(true, 100, 200, 60),
				new AppProperties.Ai(),
				new AppProperties.Supabase(false, "", "", "", "", new AppProperties.SupabaseDatabase(), new AppProperties.Storage(), new AppProperties.Realtime(false, java.util.List.of("transactions", "dashboard", "goals"))),
				new AppProperties.Monitoring(true, new AppProperties.Sentry(false, "test-dsn", "ERROR", "1.0.0", 0.1, 0.1, false, true, true), new AppProperties.HealthCheck(30000, true))
			);
		}
	}

}
