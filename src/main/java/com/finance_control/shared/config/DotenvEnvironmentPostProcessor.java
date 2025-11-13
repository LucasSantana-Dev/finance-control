package com.finance_control.shared.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * EnvironmentPostProcessor to load .env file into Spring Environment.
 * This runs early in the Spring Boot startup process, before other beans are created.
 * This ensures environment variables from .env file are available to all Spring Boot components.
 */
@Slf4j
public class DotenvEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment,
                                      org.springframework.boot.SpringApplication application) {
        loadDotenvFile(environment);
    }

    private void loadDotenvFile(ConfigurableEnvironment environment) {
        try {
            // Try multiple possible locations for .env file
            File envFile = Paths.get(".env").toFile();
            if (!envFile.exists()) {
                // Try relative to user.dir
                String userDir = System.getProperty("user.dir");
                envFile = Paths.get(userDir, ".env").toFile();
            }

            if (!envFile.exists()) {
                System.out.println("WARNING: .env file not found. Tried: " + Paths.get(".env").toAbsolutePath());
                log.warn(".env file not found in project root. Skipping dotenv loading.");
                return;
            }

            System.out.println("Loading .env file from: " + envFile.getAbsolutePath());

            Map<String, Object> envMap = new HashMap<>();

            try (BufferedReader reader = new BufferedReader(new FileReader(envFile))) {
                String line;
                int lineNumber = 0;

                while ((line = reader.readLine()) != null) {
                    lineNumber++;
                    line = line.trim();

                    // Skip empty lines and comments
                    if (line.isEmpty() || line.startsWith("#")) {
                        continue;
                    }

                    // Parse key=value format
                    int equalsIndex = line.indexOf('=');
                    if (equalsIndex > 0) {
                        String key = line.substring(0, equalsIndex).trim();
                        String value = line.substring(equalsIndex + 1).trim();

                        // Remove quotes if present
                        if ((value.startsWith("\"") && value.endsWith("\"")) ||
                            (value.startsWith("'") && value.endsWith("'"))) {
                            value = value.substring(1, value.length() - 1);
                        }

                        envMap.put(key, value);
                        log.debug("Loaded environment variable: {}={}", key, maskSensitiveValue(key, value));
                    } else {
                        log.warn("Skipping invalid line {} in .env file: {}", lineNumber, line);
                    }
                }
            }

            if (!envMap.isEmpty()) {
                MapPropertySource propertySource = new MapPropertySource("dotenv", envMap);
                environment.getPropertySources().addFirst(propertySource);
                System.out.println("SUCCESS: Loaded " + envMap.size() + " environment variables from .env file");
                log.info("Successfully loaded {} environment variables from .env file", envMap.size());

                // Debug: Print some Supabase variables
                if (envMap.containsKey("SUPABASE_DATABASE_ENABLED")) {
                    System.out.println("SUPABASE_DATABASE_ENABLED=" + envMap.get("SUPABASE_DATABASE_ENABLED"));
                }
                if (envMap.containsKey("SUPABASE_DATABASE_HOST")) {
                    System.out.println("SUPABASE_DATABASE_HOST=" + envMap.get("SUPABASE_DATABASE_HOST"));
                }
            } else {
                System.out.println("WARNING: No environment variables found in .env file");
                log.warn("No environment variables found in .env file");
            }
        } catch (IOException e) {
            log.error("Failed to load .env file: {}", e.getMessage(), e);
        }
    }

    private String maskSensitiveValue(String key, String value) {
        if (key.toLowerCase().contains("password") ||
            key.toLowerCase().contains("secret") ||
            key.toLowerCase().contains("key") ||
            key.toLowerCase().contains("token")) {
            return value != null && value.length() > 4
                ? value.substring(0, 4) + "***"
                : "***";
        }
        return value;
    }
}
