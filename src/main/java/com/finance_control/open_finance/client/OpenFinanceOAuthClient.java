package com.finance_control.open_finance.client;

import com.finance_control.open_finance.model.OpenFinanceInstitution;
import com.finance_control.shared.config.AppProperties;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/**
 * Client for Open Finance OAuth 2.0 consent flow.
 * Handles authorization URL generation, token exchange, and token refresh.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(value = "app.open-finance.enabled", havingValue = "true", matchIfMissing = false)
public class OpenFinanceOAuthClient {

    @Qualifier("openFinanceRestClient")
    private final RestClient restClient;
    private final AppProperties appProperties;

    /**
     * Generates the authorization URL for initiating the OAuth consent flow.
     *
     * @param institution the Open Finance institution
     * @param userId the user ID
     * @param state optional state parameter for CSRF protection
     * @param scopes list of requested scopes
     * @return the authorization URL
     */
    public String generateAuthorizationUrl(OpenFinanceInstitution institution, Long userId, String state, java.util.List<String> scopes) {
        com.finance_control.shared.config.properties.OpenFinanceProperties.OAuthProperties oauthConfig = appProperties.openFinance().oauth();
        String redirectUri = oauthConfig.redirectUri();
        String clientId = oauthConfig.clientId();

        if (!StringUtils.hasText(clientId)) {
            throw new IllegalStateException("Open Finance OAuth client ID is not configured");
        }

        String scopeString = scopes != null && !scopes.isEmpty() ?
                String.join(" ", scopes) :
                String.join(" ", oauthConfig.defaultScopes());

        String authUrl = institution.getAuthorizationUrl() +
                "?response_type=code" +
                "&client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8) +
                "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8) +
                "&scope=" + URLEncoder.encode(scopeString, StandardCharsets.UTF_8) +
                "&state=" + URLEncoder.encode(state != null ? state : userId.toString(), StandardCharsets.UTF_8);

        log.debug("Generated authorization URL for institution: {}", institution.getCode());
        return authUrl;
    }

    /**
     * Exchanges an authorization code for access and refresh tokens.
     *
     * @param institution the Open Finance institution
     * @param authorizationCode the authorization code from the callback
     * @param redirectUri the redirect URI used in the authorization request
     * @return token response containing access token, refresh token, and expiration
     */
    public TokenResponse exchangeAuthorizationCode(OpenFinanceInstitution institution, String authorizationCode, String redirectUri) {
        com.finance_control.shared.config.properties.OpenFinanceProperties.OAuthProperties oauthConfig = appProperties.openFinance().oauth();

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "authorization_code");
        formData.add("code", authorizationCode);
        formData.add("redirect_uri", redirectUri);
        formData.add("client_id", oauthConfig.clientId());
        formData.add("client_secret", oauthConfig.clientSecret());

        log.debug("Exchanging authorization code for tokens with institution: {}", institution.getCode());

        try {
            JsonNode response = restClient.post()
                    .uri(institution.getTokenUrl())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(formData)
                    .retrieve()
                    .body(JsonNode.class);

            TokenResponse tokenResponse = parseTokenResponse(response);
            log.info("Successfully exchanged authorization code for tokens");
            return tokenResponse;
        } catch (RestClientException e) {
            log.error("Failed to exchange authorization code: {}", e.getMessage());
            throw handleTokenError(e);
        }
    }

    /**
     * Refreshes an access token using a refresh token.
     *
     * @param institution the Open Finance institution
     * @param refreshToken the refresh token
     * @return token response with new access token and refresh token
     */
    public TokenResponse refreshToken(OpenFinanceInstitution institution, String refreshToken) {
        com.finance_control.shared.config.properties.OpenFinanceProperties.OAuthProperties oauthConfig = appProperties.openFinance().oauth();

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "refresh_token");
        formData.add("refresh_token", refreshToken);
        formData.add("client_id", oauthConfig.clientId());
        formData.add("client_secret", oauthConfig.clientSecret());

        log.debug("Refreshing access token for institution: {}", institution.getCode());

        try {
            JsonNode response = restClient.post()
                    .uri(institution.getTokenUrl())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(formData)
                    .retrieve()
                    .body(JsonNode.class);

            TokenResponse tokenResponse = parseTokenResponse(response);
            log.info("Successfully refreshed access token");
            return tokenResponse;
        } catch (RestClientException e) {
            log.error("Failed to refresh access token: {}", e.getMessage());
            throw handleTokenError(e);
        }
    }

    /**
     * Revokes a consent by invalidating the access and refresh tokens.
     *
     * @param institution the Open Finance institution
     * @param token the access token or refresh token to revoke
     * @param tokenTypeHint hint about the token type ("access_token" or "refresh_token")
     */
    public void revokeToken(OpenFinanceInstitution institution, String token, String tokenTypeHint) {
        com.finance_control.shared.config.properties.OpenFinanceProperties.OAuthProperties oauthConfig = appProperties.openFinance().oauth();

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("token", token);
        if (StringUtils.hasText(tokenTypeHint)) {
            formData.add("token_type_hint", tokenTypeHint);
        }
        formData.add("client_id", oauthConfig.clientId());
        formData.add("client_secret", oauthConfig.clientSecret());

        log.debug("Revoking token for institution: {}", institution.getCode());

        try {
            restClient.post()
                    .uri(institution.getTokenUrl().replace("/token", "/revoke"))
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(formData)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Successfully revoked token");
        } catch (RestClientException e) {
            log.error("Failed to revoke token: {}", e.getMessage());
            throw new RuntimeException("Failed to revoke token", e);
        }
    }

    private TokenResponse parseTokenResponse(JsonNode jsonNode) {
        String accessToken = jsonNode.has("access_token") ? jsonNode.get("access_token").asText() : null;
        String refreshToken = jsonNode.has("refresh_token") ? jsonNode.get("refresh_token").asText() : null;
        String tokenType = jsonNode.has("token_type") ? jsonNode.get("token_type").asText() : "Bearer";
        int expiresIn = jsonNode.has("expires_in") ? jsonNode.get("expires_in").asInt() : 3600;
        String scope = jsonNode.has("scope") ? jsonNode.get("scope").asText() : null;

        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(expiresIn);

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType(tokenType)
                .expiresIn(expiresIn)
                .expiresAt(expiresAt)
                .scope(scope)
                .build();
    }

    private RuntimeException handleTokenError(RestClientException exception) {
        HttpStatusCode statusCode = null;
        String errorBody = exception.getMessage();

        if (exception instanceof HttpClientErrorException httpEx) {
            statusCode = httpEx.getStatusCode();
            errorBody = httpEx.getResponseBodyAsString();
        } else if (exception instanceof HttpServerErrorException serverEx) {
            statusCode = serverEx.getStatusCode();
            errorBody = serverEx.getResponseBodyAsString();
        }

        log.error("Open Finance OAuth error: status={}, body={}", statusCode, errorBody);
        return new IllegalStateException("Open Finance OAuth error: " + statusCode, exception);
    }

    /**
     * Token response DTO.
     */
    @lombok.Data
    @lombok.Builder
    public static class TokenResponse {
        private String accessToken;
        private String refreshToken;
        private String tokenType;
        private int expiresIn;
        private LocalDateTime expiresAt;
        private String scope;
    }
}
