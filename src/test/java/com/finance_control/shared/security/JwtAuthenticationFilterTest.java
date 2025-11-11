package com.finance_control.shared.security;

import com.finance_control.shared.context.UserContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private UserDetails testUserDetails;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();

        testUserDetails = org.springframework.security.core.userdetails.User.builder()
                .username("1")
                .password("password")
                .authorities("ROLE_USER")
                .build();

        SecurityContextHolder.clearContext();
        UserContext.clear();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        UserContext.clear();
    }

    @Test
    void doFilterInternal_WithValidJwt_ShouldSetSecurityContext() throws Exception {
        String token = "valid-jwt-token";
        Long userId = 1L;

        request.setRequestURI("/api/transactions");
        request.addHeader("Authorization", "Bearer " + token);

        when(jwtUtils.validateToken(token)).thenReturn(true);
        when(jwtUtils.getUserIdFromToken(token)).thenReturn(userId);
        when(userDetailsService.loadUserByUsername(userId.toString())).thenReturn(testUserDetails);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isEqualTo(testUserDetails);
        assertThat(UserContext.getCurrentUserId()).isNull();
        verify(jwtUtils).validateToken(token);
        verify(jwtUtils).getUserIdFromToken(token);
        verify(userDetailsService).loadUserByUsername(userId.toString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithValidJwt_ShouldLoadUserDetails() throws Exception {
        String token = "valid-jwt-token";
        Long userId = 1L;

        request.setRequestURI("/api/users");
        request.addHeader("Authorization", "Bearer " + token);

        when(jwtUtils.validateToken(token)).thenReturn(true);
        when(jwtUtils.getUserIdFromToken(token)).thenReturn(userId);
        when(userDetailsService.loadUserByUsername(userId.toString())).thenReturn(testUserDetails);

        filter.doFilterInternal(request, response, filterChain);

        verify(userDetailsService).loadUserByUsername(userId.toString());
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
    }

    @Test
    void doFilterInternal_WithInvalidJwt_ShouldNotSetSecurityContext() throws Exception {
        String token = "invalid-jwt-token";

        request.setRequestURI("/api/transactions");
        request.addHeader("Authorization", "Bearer " + token);

        when(jwtUtils.validateToken(token)).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(jwtUtils).validateToken(token);
        verify(jwtUtils, never()).getUserIdFromToken(anyString());
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithMissingJwt_ShouldContinueWithoutAuth() throws Exception {
        request.setRequestURI("/api/transactions");

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(jwtUtils, never()).validateToken(anyString());
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithExpiredJwt_ShouldNotSetSecurityContext() throws Exception {
        String token = "expired-jwt-token";

        request.setRequestURI("/api/transactions");
        request.addHeader("Authorization", "Bearer " + token);

        when(jwtUtils.validateToken(token)).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(jwtUtils).validateToken(token);
        verify(jwtUtils, never()).getUserIdFromToken(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_ForSwaggerUiPath_ShouldSkipProcessing() throws Exception {
        request.setRequestURI("/swagger-ui/index.html");

        filter.doFilterInternal(request, response, filterChain);

        verify(jwtUtils, never()).validateToken(anyString());
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_ForAuthPath_ShouldSkipProcessing() throws Exception {
        request.setRequestURI("/auth/login");

        filter.doFilterInternal(request, response, filterChain);

        verify(jwtUtils, never()).validateToken(anyString());
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_ForActuatorPath_ShouldSkipProcessing() throws Exception {
        request.setRequestURI("/actuator/health");

        filter.doFilterInternal(request, response, filterChain);

        verify(jwtUtils, never()).validateToken(anyString());
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_AlwaysClearsUserContext() throws Exception {
        String token = "valid-jwt-token";
        Long userId = 1L;

        request.setRequestURI("/api/transactions");
        request.addHeader("Authorization", "Bearer " + token);

        when(jwtUtils.validateToken(token)).thenReturn(true);
        when(jwtUtils.getUserIdFromToken(token)).thenReturn(userId);
        when(userDetailsService.loadUserByUsername(userId.toString())).thenReturn(testUserDetails);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(UserContext.getCurrentUserId()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithException_ShouldNotThrowAndClearContext() throws Exception {
        String token = "valid-jwt-token";
        Long userId = 1L;

        request.setRequestURI("/api/transactions");
        request.addHeader("Authorization", "Bearer " + token);

        when(jwtUtils.validateToken(token)).thenReturn(true);
        when(jwtUtils.getUserIdFromToken(token)).thenReturn(userId);
        when(userDetailsService.loadUserByUsername(userId.toString())).thenThrow(new RuntimeException("Service error"));

        filter.doFilterInternal(request, response, filterChain);

        assertThat(UserContext.getCurrentUserId()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithNullUserId_ShouldNotSetSecurityContext() throws Exception {
        String token = "valid-jwt-token";

        request.setRequestURI("/api/transactions");
        request.addHeader("Authorization", "Bearer " + token);

        when(jwtUtils.validateToken(token)).thenReturn(true);
        when(jwtUtils.getUserIdFromToken(token)).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(jwtUtils).validateToken(token);
        verify(jwtUtils).getUserIdFromToken(token);
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WithoutBearerPrefix_ShouldNotProcessJwt() throws Exception {
        request.setRequestURI("/api/transactions");
        request.addHeader("Authorization", "token-without-bearer");

        filter.doFilterInternal(request, response, filterChain);

        verify(jwtUtils, never()).validateToken(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_ForApiDocsPath_ShouldSkipProcessing() throws Exception {
        request.setRequestURI("/v3/api-docs");

        filter.doFilterInternal(request, response, filterChain);

        verify(jwtUtils, never()).validateToken(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_ForPublicPath_ShouldSkipProcessing() throws Exception {
        request.setRequestURI("/public/endpoint");

        filter.doFilterInternal(request, response, filterChain);

        verify(jwtUtils, never()).validateToken(anyString());
        verify(filterChain).doFilter(request, response);
    }
}
