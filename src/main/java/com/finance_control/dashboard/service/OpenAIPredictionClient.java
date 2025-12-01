package com.finance_control.dashboard.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.finance_control.shared.config.AppProperties;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Implementation of {@link PredictionModelClient} that communicates with OpenAI's Responses API.
 * This is the primary (default) provider when both OpenAI and CometAPI are configured.
 */
@Service
@Primary
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "app.ai.openai", name = "enabled", havingValue = "true")
public class OpenAIPredictionClient implements PredictionModelClient {

    private static final String RESPONSES_ENDPOINT = "/responses";

    private final AppProperties appProperties;

    @Override
    public String generateForecast(String prompt) {
        com.finance_control.shared.config.properties.AiProperties aiConfig = appProperties.ai();
        if (aiConfig == null || aiConfig.openai() == null || !aiConfig.openai().enabled()) {
            throw new IllegalStateException("OpenAI predictions are not enabled");
        }

        if (!StringUtils.hasText(aiConfig.openai().apiKey())) {
            throw new IllegalStateException("OpenAI API key must be configured before requesting predictions");
        }

        RestClient client = RestClient.builder()
                .baseUrl(aiConfig.openai().baseUrl())
                .build();

        Map<String, Object> payload = new HashMap<>();
        payload.put("model", aiConfig.openai().model());
        payload.put("input", prompt);
        if (aiConfig.openai().maxTokens() != null) {
            payload.put("max_output_tokens", aiConfig.openai().maxTokens());
        }
        if (aiConfig.openai().temperature() != null) {
            payload.put("temperature", aiConfig.openai().temperature());
        }

        try {
            JsonNode responseNode = client.post()
                    .uri(RESPONSES_ENDPOINT)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + aiConfig.openai().apiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(JsonNode.class);

            if (responseNode == null) {
                log.warn("OpenAI response returned null payload");
                return "";
            }

            return extractText(responseNode);
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            log.error("OpenAI API error: status={} body={}", ex.getStatusCode().value(), ex.getResponseBodyAsString());
            throw new IllegalStateException("OpenAI API returned an error: " + ex.getStatusText(), ex);
        } catch (RestClientException ex) {
            log.error("Unexpected error retrieving prediction from OpenAI: {}", ex.getMessage());
            throw new IllegalStateException("Failed to obtain financial prediction from OpenAI", ex);
        }
    }

    private String extractText(JsonNode root) {
        if (root == null || root.isMissingNode()) {
            return "";
        }

        JsonNode outputNode = root.path("output");
        if (!outputNode.isArray() || outputNode.isEmpty()) {
            return root.toString();
        }

        String text = StreamSupport.stream(outputNode.spliterator(), false)
                .flatMap(output -> {
                    JsonNode content = output.path("content");
                    if (!content.isArray()) {
                        return Stream.of(content);
                    }
                    return StreamSupport.stream(content.spliterator(), false);
                })
                .filter(Objects::nonNull)
                .filter(content -> "text".equals(content.path("type").asText()))
                .map(content -> content.path("text").path("value").asText())
                .filter(StringUtils::hasText)
                .collect(Collectors.joining("\n"))
                .trim();

        return StringUtils.hasText(text) ? text : root.toString();
    }
}
