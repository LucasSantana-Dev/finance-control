package com.finance_control.dashboard.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.finance_control.shared.config.AppProperties;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Implementation of {@link PredictionModelClient} that communicates with CometAPI's OpenAI-compatible API.
 * CometAPI provides access to 500+ AI models through a unified OpenAI-compatible interface.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "app.ai.cometapi", name = "enabled", havingValue = "true")
public class CometAPIPredictionClient implements PredictionModelClient {

    private static final String CHAT_COMPLETIONS_ENDPOINT = "/chat/completions";

    private final AppProperties appProperties;

    @Override
    public String generateForecast(String prompt) {
        com.finance_control.shared.config.properties.AiProperties aiConfig = appProperties.ai();
        if (aiConfig == null || aiConfig.cometapi() == null || !aiConfig.cometapi().enabled()) {
            throw new IllegalStateException("CometAPI predictions are not enabled");
        }

        if (!StringUtils.hasText(aiConfig.cometapi().apiKey())) {
            throw new IllegalStateException("CometAPI API key must be configured before requesting predictions");
        }

        RestClient client = RestClient.builder()
                .baseUrl(aiConfig.cometapi().baseUrl())
                .build();

        Map<String, Object> payload = buildRequestPayload(prompt, aiConfig.cometapi());

        try {
            log.debug("Sending request to CometAPI: endpoint={}, model={}",
                    CHAT_COMPLETIONS_ENDPOINT, aiConfig.cometapi().model());

            JsonNode responseNode = client.post()
                    .uri(CHAT_COMPLETIONS_ENDPOINT)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + aiConfig.cometapi().apiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(JsonNode.class);

            if (responseNode == null) {
                log.warn("CometAPI response returned null payload");
                return "";
            }

            String extractedText = extractText(responseNode);
            log.debug("CometAPI response extracted successfully, length={}", extractedText.length());
            return extractedText;

        } catch (HttpClientErrorException ex) {
            log.error("CometAPI API client error: status={} body={}",
                    ex.getStatusCode().value(), ex.getResponseBodyAsString());

            if (ex.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                throw new IllegalStateException("CometAPI rate limit exceeded. Please try again later.", ex);
            } else if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new IllegalStateException("CometAPI authentication failed. Please check your API key.", ex);
            }

            throw new IllegalStateException("CometAPI API returned an error: " + ex.getStatusText(), ex);
        } catch (HttpServerErrorException ex) {
            log.error("CometAPI API server error: status={} body={}",
                    ex.getStatusCode().value(), ex.getResponseBodyAsString());
            throw new IllegalStateException("CometAPI service is temporarily unavailable. Please try again later.", ex);
        } catch (RestClientException ex) {
            log.error("Unexpected error retrieving prediction from CometAPI: {}", ex.getMessage());
            throw new IllegalStateException("Failed to obtain financial prediction from CometAPI", ex);
        }
    }

    private Map<String, Object> buildRequestPayload(String prompt,
            com.finance_control.shared.config.properties.AiProperties.CometAPIProperties config) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("model", config.model());

        List<Map<String, String>> messages = new ArrayList<>();

        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", "You are a financial planning assistant. Provide accurate financial predictions and recommendations based on the provided data.");
        messages.add(systemMessage);

        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);
        messages.add(userMessage);

        payload.put("messages", messages);

        if (config.maxTokens() != null) {
            payload.put("max_tokens", config.maxTokens());
        }
        if (config.temperature() != null) {
            payload.put("temperature", config.temperature());
        }

        return payload;
    }

    private String extractText(JsonNode root) {
        if (root == null || root.isMissingNode()) {
            return "";
        }

        JsonNode choicesNode = root.path("choices");
        if (!choicesNode.isArray() || choicesNode.isEmpty()) {
            log.warn("CometAPI response missing choices array, returning full response");
            return root.toString();
        }

        JsonNode firstChoice = choicesNode.get(0);
        if (firstChoice == null || firstChoice.isMissingNode()) {
            log.warn("CometAPI response first choice is missing, returning full response");
            return root.toString();
        }

        JsonNode messageNode = firstChoice.path("message");
        if (messageNode.isMissingNode()) {
            log.warn("CometAPI response message is missing, returning full response");
            return root.toString();
        }

        JsonNode contentNode = messageNode.path("content");
        if (contentNode.isMissingNode() || !contentNode.isTextual()) {
            log.warn("CometAPI response content is missing or not textual, returning full response");
            return root.toString();
        }

        String content = contentNode.asText();
        return StringUtils.hasText(content) ? content.trim() : "";
    }
}
