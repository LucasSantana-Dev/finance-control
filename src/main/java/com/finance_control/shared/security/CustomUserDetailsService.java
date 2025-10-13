package com.finance_control.shared.security;

import com.finance_control.users.model.User;
import com.finance_control.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Custom UserDetailsService implementation that loads user details by user ID
 * for JWT authentication purposes.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    
    private final UserRepository userRepository;
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            Long userId = Long.valueOf(username);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + userId));
            
            return createUserDetails(user);
        } catch (NumberFormatException e) {
            throw new UsernameNotFoundException("Invalid user ID format: " + username);
        }
    }
    
    /**
     * Creates UserDetails from a User entity.
     * 
     * @param user the user entity
     * @return UserDetails object
     */
    private UserDetails createUserDetails(User user) {
        return new CustomUserDetails(user);
    }
} 