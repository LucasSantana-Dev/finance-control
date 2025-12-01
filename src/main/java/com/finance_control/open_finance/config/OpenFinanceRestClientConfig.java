package com.finance_control.open_finance.config;

import com.finance_control.shared.config.AppProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.io.SocketConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import org.apache.hc.core5.util.TimeValue;

/**
 * Configuration for Open Finance RestClient.
 * Creates RestClient instances configured with mTLS for Open Finance API calls.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(value = "app.open-finance.enabled", havingValue = "true", matchIfMissing = false)
public class OpenFinanceRestClientConfig {

        private final AppProperties appProperties;
        private final OpenFinanceCertificateConfig certificateConfig;

        /**
         * Creates a RestClient bean configured for Open Finance API calls with mTLS.
         *
         * @return configured RestClient
         */
        @Bean
        @Qualifier("openFinanceRestClient")
        @ConditionalOnProperty(value = "app.open-finance.enabled", havingValue = "true", matchIfMissing = false)
        public RestClient openFinanceRestClient() {
                String baseUrl = appProperties.openFinance().useProduction()
                                ? appProperties.openFinance().productionBaseUrl()
                                : appProperties.openFinance().sandboxBaseUrl();

                if (!StringUtils.hasText(baseUrl)) {
                        log.warn("Open Finance base URL not configured");
                        return RestClient.builder().build();
                }

                log.info("Configuring Open Finance RestClient with base URL: {}", baseUrl);

                try {
                        javax.net.ssl.SSLContext sslContext = certificateConfig.openFinanceJavaSslContext();

                        SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(
                                        sslContext,
                                        (hostname, session) -> true);

                        HttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder
                                        .create()
                                        .setSSLSocketFactory(sslSocketFactory)
                                        .setDefaultSocketConfig(SocketConfig.custom()
                                                        .setSoTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                                                        .build())
                                        .build();

                        CloseableHttpClient httpClient = HttpClients.custom()
                                        .setConnectionManager(connectionManager)
                                        .evictIdleConnections(TimeValue.ofSeconds(30))
                                        .evictExpiredConnections()
                                        .build();

                        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(
                                        httpClient);
                        requestFactory.setConnectTimeout(java.time.Duration.ofSeconds(30));
                        requestFactory.setConnectionRequestTimeout(java.time.Duration.ofSeconds(30));

                        return RestClient.builder()
                                        .baseUrl(baseUrl)
                                        .requestFactory(requestFactory)
                                        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                        .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                                        .build();
                } catch (Exception e) {
                        log.error("Failed to configure RestClient with SSL context", e);
                        return RestClient.builder()
                                        .baseUrl(baseUrl)
                                        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                        .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                                        .build();
                }
        }
}
