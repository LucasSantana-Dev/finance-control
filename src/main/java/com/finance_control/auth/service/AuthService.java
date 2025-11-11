package com.finance_control.auth.service;

import com.finance_control.auth.exception.AuthenticationException;
import com.finance_control.shared.monitoring.MetricsService;
import com.finance_control.users.model.User;
import com.finance_control.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MetricsService metricsService;

    /**
     * Authenticates a user by email and password.
     *
     * @param email the user's email
     * @param password the user's password
     * @return the user ID if authentication is successful
     * @throws RuntimeException if authentication fails
     */
    public Long authenticate(String email, String password) {
        var sample = metricsService.startAuthenticationTimer();
        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new AuthenticationException("Invalid email or password"));

            if (Boolean.FALSE.equals(user.getIsActive())) {
                throw new AuthenticationException("User account is disabled");
            }

            if (!passwordEncoder.matches(password, user.getPassword())) {
                throw new AuthenticationException("Invalid email or password");
            }

            metricsService.incrementUserLogin();
            // Avoid logging PII such as email at INFO level
            log.info("User authenticated successfully");
            return user.getId();
        } finally {
            metricsService.recordAuthenticationTime(sample);
        }
    }

    /**
     * Changes the password for the current authenticated user.
     *
     * @param currentPassword the current password for validation
     * @param newPassword the new password to set
     * @throws AuthenticationException if current password is invalid
     */
    public void changePassword(String currentPassword, String newPassword) {
        // TODO: Get current user from security context
        // For now, this is a placeholder implementation
        log.info("Password change requested");

        // TODO: Implement with proper security context to get current user and validate current password
        throw new UnsupportedOperationException("Password change not yet implemented - requires security context integration");
    }


}
