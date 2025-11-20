package com.finance_control.shared.config.properties;

/**
 * AI configuration properties.
 */
public record AiProperties(
    boolean enabled,
    OpenAIProperties openai
) {
    public AiProperties() {
        this(false, new OpenAIProperties());
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
}

