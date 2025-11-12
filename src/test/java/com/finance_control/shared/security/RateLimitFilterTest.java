package com.finance_control.shared.security;

import com.finance_control.shared.config.AppProperties;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RateLimitFilterTest {

    @Mock
    private Bucket rateLimitBucket;

    @Mock
    private AppProperties appProperties;

    @Mock
    private AppProperties.Security security;

    @Mock
    private AppProperties.RateLimit rateLimit;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private RateLimitFilter filter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();

        when(appProperties.rateLimit()).thenReturn(rateLimit);
        when(appProperties.security()).thenReturn(security);
    }

    @Test
    void doFilterInternal_WhenRateLimitDisabled_ShouldSkipFiltering() throws Exception {
        request.setRequestURI("/api/transactions");

        AppProperties.RateLimit disabledRateLimit = new AppProperties.RateLimit(false, 100, 200, 60);
        when(appProperties.rateLimit()).thenReturn(disabledRateLimit);

        filter.doFilterInternal(request, response, filterChain);

        verify(rateLimitBucket, never()).tryConsumeAndReturnRemaining(anyInt());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_ForPublicEndpoint_ShouldSkipFiltering() throws Exception {
        request.setRequestURI("/api/auth/login");

        AppProperties.RateLimit enabledRateLimit = new AppProperties.RateLimit(true, 100, 200, 60);
        when(appProperties.rateLimit()).thenReturn(enabledRateLimit);
        AppProperties.Security testSecurity = new AppProperties.Security(
            new AppProperties.Jwt("secret", 86400000L, 604800000L, "test", "test"),
            new AppProperties.Cors(List.of(), List.of(), List.of(), false, 0),
            List.of("/api/auth/**", "/api/users")
        );
        when(appProperties.security()).thenReturn(testSecurity);

        filter.doFilterInternal(request, response, filterChain);

        verify(rateLimitBucket, never()).tryConsumeAndReturnRemaining(anyInt());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithAvailableTokens_ShouldAllowRequest() throws Exception {
        request.setRequestURI("/api/transactions");

        ConsumptionProbe probe = mock(ConsumptionProbe.class);
        when(probe.isConsumed()).thenReturn(true);
        when(probe.getRemainingTokens()).thenReturn(99L);
        when(probe.getNanosToWaitForRefill()).thenReturn(0L);

        AppProperties.RateLimit enabledRateLimit = new AppProperties.RateLimit(true, 100, 200, 60);
        when(appProperties.rateLimit()).thenReturn(enabledRateLimit);
        AppProperties.Security testSecurity = new AppProperties.Security(
            new AppProperties.Jwt("secret", 86400000L, 604800000L, "test", "test"),
            new AppProperties.Cors(List.of(), List.of(), List.of(), false, 0),
            List.of("/api/auth/**")
        );
        when(appProperties.security()).thenReturn(testSecurity);
        when(rateLimitBucket.tryConsumeAndReturnRemaining(1)).thenReturn(probe);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(200);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithAvailableTokens_ShouldSetRateLimitHeaders() throws Exception {
        request.setRequestURI("/api/transactions");

        ConsumptionProbe probe = mock(ConsumptionProbe.class);
        when(probe.isConsumed()).thenReturn(true);
        when(probe.getRemainingTokens()).thenReturn(50L);
        when(probe.getNanosToWaitForRefill()).thenReturn(0L);

        AppProperties.RateLimit enabledRateLimit = new AppProperties.RateLimit(true, 100, 200, 60);
        when(appProperties.rateLimit()).thenReturn(enabledRateLimit);
        AppProperties.Security testSecurity = new AppProperties.Security(
            new AppProperties.Jwt("secret", 86400000L, 604800000L, "test", "test"),
            new AppProperties.Cors(List.of(), List.of(), List.of(), false, 0),
            List.of("/api/auth/**")
        );
        when(appProperties.security()).thenReturn(testSecurity);
        when(rateLimitBucket.tryConsumeAndReturnRemaining(1)).thenReturn(probe);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(response.getHeader("X-Rate-Limit-Remaining")).isEqualTo("50");
        assertThat(response.getHeader("X-Rate-Limit-Reset")).isNotNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WhenRateLimitExceeded_ShouldReturn429() throws Exception {
        request.setRequestURI("/api/transactions");

        ConsumptionProbe probe = mock(ConsumptionProbe.class);
        when(probe.isConsumed()).thenReturn(false);
        when(probe.getRemainingTokens()).thenReturn(0L);
        when(probe.getNanosToWaitForRefill()).thenReturn(1_000_000_000L); // 1 second

        AppProperties.RateLimit enabledRateLimit = new AppProperties.RateLimit(true, 100, 200, 60);
        when(appProperties.rateLimit()).thenReturn(enabledRateLimit);
        AppProperties.Security testSecurity = new AppProperties.Security(
            new AppProperties.Jwt("secret", 86400000L, 604800000L, "test", "test"),
            new AppProperties.Cors(List.of(), List.of(), List.of(), false, 0),
            List.of("/api/auth/**")
        );
        when(appProperties.security()).thenReturn(testSecurity);
        when(rateLimitBucket.tryConsumeAndReturnRemaining(1)).thenReturn(probe);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(429);
        assertThat(response.getHeader("X-Rate-Limit-Remaining")).isEqualTo("0");
        assertThat(response.getHeader("Retry-After")).isNotNull();
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WhenRateLimitExceeded_ShouldSetRetryAfterHeader() throws Exception {
        request.setRequestURI("/api/transactions");

        ConsumptionProbe probe = mock(ConsumptionProbe.class);
        when(probe.isConsumed()).thenReturn(false);
        when(probe.getNanosToWaitForRefill()).thenReturn(5_000_000_000L); // 5 seconds

        AppProperties.RateLimit enabledRateLimit = new AppProperties.RateLimit(true, 100, 200, 60);
        when(appProperties.rateLimit()).thenReturn(enabledRateLimit);
        AppProperties.Security testSecurity = new AppProperties.Security(
            new AppProperties.Jwt("secret", 86400000L, 604800000L, "test", "test"),
            new AppProperties.Cors(List.of(), List.of(), List.of(), false, 0),
            List.of("/api/auth/**")
        );
        when(appProperties.security()).thenReturn(testSecurity);
        when(rateLimitBucket.tryConsumeAndReturnRemaining(1)).thenReturn(probe);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(response.getHeader("Retry-After")).isEqualTo("5");
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WhenRateLimitExceeded_ShouldWriteErrorJson() throws Exception {
        request.setRequestURI("/api/transactions");

        ConsumptionProbe probe = mock(ConsumptionProbe.class);
        when(probe.isConsumed()).thenReturn(false);
        when(probe.getNanosToWaitForRefill()).thenReturn(1_000_000_000L);

        AppProperties.RateLimit enabledRateLimit = new AppProperties.RateLimit(true, 100, 200, 60);
        when(appProperties.rateLimit()).thenReturn(enabledRateLimit);
        AppProperties.Security testSecurity = new AppProperties.Security(
            new AppProperties.Jwt("secret", 86400000L, 604800000L, "test", "test"),
            new AppProperties.Cors(List.of(), List.of(), List.of(), false, 0),
            List.of("/api/auth/**")
        );
        when(appProperties.security()).thenReturn(testSecurity);
        when(rateLimitBucket.tryConsumeAndReturnRemaining(1)).thenReturn(probe);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(response.getContentType()).isEqualTo("application/json");
        assertThat(response.getContentAsString()).contains("Rate limit exceeded");
        assertThat(response.getContentAsString()).contains("Too many requests");
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void doFilterInternal_ShouldExtractClientIpFromHeaders() throws Exception {
        request.setRequestURI("/api/transactions");
        request.addHeader("X-Forwarded-For", "192.168.1.1");
        request.setRemoteAddr("10.0.0.1");

        ConsumptionProbe probe = mock(ConsumptionProbe.class);
        when(probe.isConsumed()).thenReturn(false);
        when(probe.getNanosToWaitForRefill()).thenReturn(1_000_000_000L);

        AppProperties.RateLimit enabledRateLimit = new AppProperties.RateLimit(true, 100, 200, 60);
        when(appProperties.rateLimit()).thenReturn(enabledRateLimit);
        AppProperties.Security testSecurity = new AppProperties.Security(
            new AppProperties.Jwt("secret", 86400000L, 604800000L, "test", "test"),
            new AppProperties.Cors(List.of(), List.of(), List.of(), false, 0),
            List.of("/api/auth/**")
        );
        when(appProperties.security()).thenReturn(testSecurity);
        when(rateLimitBucket.tryConsumeAndReturnRemaining(1)).thenReturn(probe);

        filter.doFilterInternal(request, response, filterChain);

        verify(rateLimitBucket).tryConsumeAndReturnRemaining(1);
    }

    @Test
    void doFilterInternal_ShouldExtractClientIpFromXRealIp() throws Exception {
        request.setRequestURI("/api/transactions");
        request.addHeader("X-Real-IP", "192.168.1.2");
        request.setRemoteAddr("10.0.0.1");

        ConsumptionProbe probe = mock(ConsumptionProbe.class);
        when(probe.isConsumed()).thenReturn(false);
        when(probe.getNanosToWaitForRefill()).thenReturn(1_000_000_000L);

        AppProperties.RateLimit enabledRateLimit = new AppProperties.RateLimit(true, 100, 200, 60);
        when(appProperties.rateLimit()).thenReturn(enabledRateLimit);
        AppProperties.Security testSecurity = new AppProperties.Security(
            new AppProperties.Jwt("secret", 86400000L, 604800000L, "test", "test"),
            new AppProperties.Cors(List.of(), List.of(), List.of(), false, 0),
            List.of("/api/auth/**")
        );
        when(appProperties.security()).thenReturn(testSecurity);
        when(rateLimitBucket.tryConsumeAndReturnRemaining(1)).thenReturn(probe);

        filter.doFilterInternal(request, response, filterChain);

        verify(rateLimitBucket).tryConsumeAndReturnRemaining(1);
    }

    @Test
    void doFilterInternal_ShouldUseRemoteAddrWhenNoHeaders() throws Exception {
        request.setRequestURI("/api/transactions");
        request.setRemoteAddr("192.168.1.3");

        ConsumptionProbe probe = mock(ConsumptionProbe.class);
        when(probe.isConsumed()).thenReturn(false);
        when(probe.getNanosToWaitForRefill()).thenReturn(1_000_000_000L);

        AppProperties.RateLimit enabledRateLimit = new AppProperties.RateLimit(true, 100, 200, 60);
        when(appProperties.rateLimit()).thenReturn(enabledRateLimit);
        AppProperties.Security testSecurity = new AppProperties.Security(
            new AppProperties.Jwt("secret", 86400000L, 604800000L, "test", "test"),
            new AppProperties.Cors(List.of(), List.of(), List.of(), false, 0),
            List.of("/api/auth/**")
        );
        when(appProperties.security()).thenReturn(testSecurity);
        when(rateLimitBucket.tryConsumeAndReturnRemaining(1)).thenReturn(probe);

        filter.doFilterInternal(request, response, filterChain);

        verify(rateLimitBucket).tryConsumeAndReturnRemaining(1);
    }

    @Test
    void doFilterInternal_ForMultiplePublicEndpoints_ShouldSkipFiltering() throws Exception {
        request.setRequestURI("/api/users");

        AppProperties.RateLimit enabledRateLimit = new AppProperties.RateLimit(true, 100, 200, 60);
        when(appProperties.rateLimit()).thenReturn(enabledRateLimit);
        AppProperties.Security testSecurity = new AppProperties.Security(
            new AppProperties.Jwt("secret", 86400000L, 604800000L, "test", "test"),
            new AppProperties.Cors(List.of(), List.of(), List.of(), false, 0),
            List.of("/api/auth/**", "/api/users", "/api/monitoring/**")
        );
        when(appProperties.security()).thenReturn(testSecurity);

        filter.doFilterInternal(request, response, filterChain);

        verify(rateLimitBucket, never()).tryConsumeAndReturnRemaining(anyInt());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WhenPublicEndpointsNull_ShouldProcessRequest() throws Exception {
        request.setRequestURI("/api/transactions");

        ConsumptionProbe probe = mock(ConsumptionProbe.class);
        when(probe.isConsumed()).thenReturn(true);
        when(probe.getRemainingTokens()).thenReturn(99L);
        when(probe.getNanosToWaitForRefill()).thenReturn(0L);

        AppProperties.RateLimit enabledRateLimit = new AppProperties.RateLimit(true, 100, 200, 60);
        when(appProperties.rateLimit()).thenReturn(enabledRateLimit);
        AppProperties.Security testSecurity = new AppProperties.Security(
            new AppProperties.Jwt("secret", 86400000L, 604800000L, "test", "test"),
            new AppProperties.Cors(List.of(), List.of(), List.of(), false, 0),
            null
        );
        when(appProperties.security()).thenReturn(testSecurity);
        when(rateLimitBucket.tryConsumeAndReturnRemaining(1)).thenReturn(probe);

        filter.doFilterInternal(request, response, filterChain);

        verify(rateLimitBucket).tryConsumeAndReturnRemaining(1);
        verify(filterChain).doFilter(request, response);
    }
}
