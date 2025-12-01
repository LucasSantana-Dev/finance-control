package com.finance_control.shared.config.properties;

/**
 * AI configuration properties.
 */
public record AiProperties(
    boolean enabled,
    String provider,
    OpenAIProperties openai,
    CometAPIProperties cometapi
) {
    public AiProperties() {
        this(false, "openai", new OpenAIProperties(), new CometAPIProperties());
    }

    public record OpenAIProperties(
        boolean enabled,
        String apiKey,
        String model,
        Integer maxTokens,
        Double temperature,
        String baseUrl
    ) {
        public OpenAIProperties() {
            this(false, "", "gpt-4o-mini", 800, 0.2, "https://api.openai.com/v1");
        }
    }

    public record CometAPIProperties(
        boolean enabled,
        String apiKey,
        String model,
        Integer maxTokens,
        Double temperature,
        String baseUrl
    ) {
        public CometAPIProperties() {
            this(false, "", "gpt-4o-mini", 800, 0.7, "https://api.cometapi.com/v1");
        }
    }
}
