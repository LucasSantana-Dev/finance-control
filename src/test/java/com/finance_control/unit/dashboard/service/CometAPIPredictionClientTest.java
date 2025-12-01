package com.finance_control.unit.dashboard.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance_control.dashboard.service.CometAPIPredictionClient;
import com.finance_control.shared.config.AppProperties;
import com.finance_control.shared.config.properties.AiProperties;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CometAPIPredictionClientTest {

    @Mock
    private AppProperties appProperties;

    private CometAPIPredictionClient client;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        client = new CometAPIPredictionClient(appProperties);
    }

    @Test
    void generateForecast_ShouldThrowException_WhenCometAPIIsNotEnabled() {
        AiProperties aiConfig = new AiProperties(
                true,
                "cometapi",
                new AiProperties.OpenAIProperties(),
                new AiProperties.CometAPIProperties(false, "", "", null, null, "")
        );
        when(appProperties.ai()).thenReturn(aiConfig);

        assertThatThrownBy(() -> client.generateForecast("test prompt"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("CometAPI predictions are not enabled");
    }

    @Test
    void generateForecast_ShouldThrowException_WhenApiKeyIsMissing() {
        AiProperties aiConfig = new AiProperties(
                true,
                "cometapi",
                new AiProperties.OpenAIProperties(),
                new AiProperties.CometAPIProperties(true, "", "gpt-4o-mini", 800, 0.7, "https://api.cometapi.com/v1")
        );
        when(appProperties.ai()).thenReturn(aiConfig);

        assertThatThrownBy(() -> client.generateForecast("test prompt"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("CometAPI API key must be configured");
    }

    @Test
    void generateForecast_ShouldThrowException_WhenAiConfigIsNull() {
        when(appProperties.ai()).thenReturn(null);

        assertThatThrownBy(() -> client.generateForecast("test prompt"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("CometAPI predictions are not enabled");
    }

    @Test
    void generateForecast_ShouldThrowException_WhenCometAPIConfigIsNull() {
        AiProperties aiConfig = new AiProperties(
                true,
                "cometapi",
                new AiProperties.OpenAIProperties(),
                null
        );
        when(appProperties.ai()).thenReturn(aiConfig);

        assertThatThrownBy(() -> client.generateForecast("test prompt"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("CometAPI predictions are not enabled");
    }

    @Test
    void buildRequestPayload_ShouldIncludeSystemAndUserMessages() {
        AiProperties.CometAPIProperties cometapiConfig = new AiProperties.CometAPIProperties(
                true,
                "test-api-key",
                "gpt-4o-mini",
                800,
                0.7,
                "https://api.cometapi.com/v1"
        );

        String testPrompt = "Analyze these financial data...";

        try {
            java.lang.reflect.Method method = CometAPIPredictionClient.class.getDeclaredMethod(
                    "buildRequestPayload", String.class, AiProperties.CometAPIProperties.class);
            method.setAccessible(true);

            @SuppressWarnings("unchecked")
            Map<String, Object> payload = (Map<String, Object>) method.invoke(client, testPrompt, cometapiConfig);

            assertThat(payload).isNotNull();
            assertThat(payload.get("model")).isEqualTo("gpt-4o-mini");
            assertThat(payload.get("max_tokens")).isEqualTo(800);
            assertThat(payload.get("temperature")).isEqualTo(0.7);

            @SuppressWarnings("unchecked")
            List<Map<String, String>> messages = (List<Map<String, String>>) payload.get("messages");
            assertThat(messages).hasSize(2);
            assertThat(messages.get(0).get("role")).isEqualTo("system");
            assertThat(messages.get(0).get("content")).contains("financial planning assistant");
            assertThat(messages.get(1).get("role")).isEqualTo("user");
            assertThat(messages.get(1).get("content")).isEqualTo(testPrompt);
        } catch (Exception e) {
            throw new RuntimeException("Failed to test buildRequestPayload", e);
        }
    }

    @Test
    void extractText_ShouldExtractContentFromValidResponse() throws Exception {
        String jsonResponse = """
                {
                  "choices": [
                    {
                      "message": {
                        "content": "This is the AI response content"
                      }
                    }
                  ]
                }
                """;

        JsonNode responseNode = objectMapper.readTree(jsonResponse);

        try {
            java.lang.reflect.Method method = CometAPIPredictionClient.class.getDeclaredMethod(
                    "extractText", JsonNode.class);
            method.setAccessible(true);

            String extracted = (String) method.invoke(client, responseNode);

            assertThat(extracted).isEqualTo("This is the AI response content");
        } catch (Exception e) {
            throw new RuntimeException("Failed to test extractText", e);
        }
    }

    @Test
    void extractText_ShouldHandleMissingChoices() throws Exception {
        String jsonResponse = """
                {
                  "error": "Rate limit exceeded"
                }
                """;

        JsonNode responseNode = objectMapper.readTree(jsonResponse);

        try {
            java.lang.reflect.Method method = CometAPIPredictionClient.class.getDeclaredMethod(
                    "extractText", JsonNode.class);
            method.setAccessible(true);

            String extracted = (String) method.invoke(client, responseNode);

            assertThat(extracted).isNotEmpty();
            assertThat(extracted).contains("error");
        } catch (Exception e) {
            throw new RuntimeException("Failed to test extractText with missing choices", e);
        }
    }

    @Test
    void extractText_ShouldHandleEmptyChoices() throws Exception {
        String jsonResponse = """
                {
                  "choices": []
                }
                """;

        JsonNode responseNode = objectMapper.readTree(jsonResponse);

        try {
            java.lang.reflect.Method method = CometAPIPredictionClient.class.getDeclaredMethod(
                    "extractText", JsonNode.class);
            method.setAccessible(true);

            String extracted = (String) method.invoke(client, responseNode);

            assertThat(extracted).isNotEmpty();
            assertThat(extracted).contains("choices");
        } catch (Exception e) {
            throw new RuntimeException("Failed to test extractText with empty choices", e);
        }
    }

    @Test
    void extractText_ShouldHandleNullResponse() {
        try {
            java.lang.reflect.Method method = CometAPIPredictionClient.class.getDeclaredMethod(
                    "extractText", JsonNode.class);
            method.setAccessible(true);

            String extracted = (String) method.invoke(client, (JsonNode) null);

            assertThat(extracted).isEmpty();
        } catch (Exception e) {
            throw new RuntimeException("Failed to test extractText with null", e);
        }
    }
}
