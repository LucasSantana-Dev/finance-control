package com.finance_control.open_finance.config;

import com.finance_control.shared.config.AppProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

/**
 * Configuration for Open Finance WebClient.
 * Creates WebClient instances configured with mTLS for Open Finance API calls.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(value = "app.open-finance.enabled", havingValue = "true", matchIfMissing = false)
public class OpenFinanceWebClientConfig {

    private final AppProperties appProperties;
    private final OpenFinanceCertificateConfig certificateConfig;

    /**
     * Creates a WebClient bean configured for Open Finance API calls with mTLS.
     *
     * @return configured WebClient
     */
    @Bean
    @Qualifier("openFinanceWebClient")
    @ConditionalOnProperty(value = "app.open-finance.enabled", havingValue = "true", matchIfMissing = false)
    public WebClient openFinanceWebClient() {
        String baseUrl = appProperties.openFinance().useProduction() ?
                appProperties.openFinance().productionBaseUrl() :
                appProperties.openFinance().sandboxBaseUrl();

        if (!StringUtils.hasText(baseUrl)) {
            log.warn("Open Finance base URL not configured");
            return WebClient.builder().build();
        }

        log.info("Configuring Open Finance WebClient with base URL: {}", baseUrl);

        HttpClient httpClient = HttpClient.create()
                .secure(sslContextSpec -> sslContextSpec.sslContext(certificateConfig.openFinanceSslContext()))
                .responseTimeout(Duration.ofSeconds(30));

        return WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();
    }
}
